package com.lolabible.app.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Reads text aloud with the phone's built-in text-to-speech engine (works offline once the
 * voice is installed). The engine is only started the first time something is spoken.
 *
 * Call [shutdown] when the owner is destroyed to release the engine.
 */
class Speaker(context: Context) {

    enum class Event { VoiceUnavailable }

    private val appContext = context.applicationContext
    private var engine: TextToSpeech? = null
    private var engineReady = false
    private var pendingRequest: Request? = null

    /** Id of the final utterance of the current request; reaching it means reading is done. */
    @Volatile private var finalUtteranceId: String? = null
    private var requestCounter = 0

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()

    private data class Request(val lines: List<String>, val locales: List<Locale>)

    /** Reads [lines] in order, replacing anything currently being read. */
    fun speak(lines: List<String>, locales: List<Locale>) {
        stop()
        val request = Request(lines, locales)
        _isSpeaking.value = true
        val current = engine
        when {
            current == null -> {
                pendingRequest = request
                engine = TextToSpeech(appContext, ::onEngineInit)
            }
            !engineReady -> pendingRequest = request
            else -> start(current, request)
        }
    }

    fun stop() {
        pendingRequest = null
        finalUtteranceId = null
        engine?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        engine?.shutdown()
        engine = null
        engineReady = false
    }

    private fun onEngineInit(status: Int) {
        val current = engine ?: return
        val request = pendingRequest
        pendingRequest = null
        if (status != TextToSpeech.SUCCESS) {
            current.shutdown()
            engine = null
            fail()
            return
        }
        engineReady = true
        current.setOnUtteranceProgressListener(progressListener)
        if (request != null) start(current, request)
    }

    private fun start(engine: TextToSpeech, request: Request) {
        val locale = request.locales.firstOrNull { engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
        if (locale == null || engine.setLanguage(locale) < TextToSpeech.LANG_AVAILABLE || request.lines.isEmpty()) {
            fail()
            return
        }
        val prefix = "r${++requestCounter}-"
        finalUtteranceId = prefix + request.lines.lastIndex
        request.lines.forEachIndexed { index, line ->
            engine.speak(line, TextToSpeech.QUEUE_ADD, null, prefix + index)
        }
    }

    private fun fail() {
        _isSpeaking.value = false
        _events.tryEmit(Event.VoiceUnavailable)
    }

    // Called on a background thread by the engine.
    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) {
            if (utteranceId != null && utteranceId == finalUtteranceId) {
                finalUtteranceId = null
                _isSpeaking.value = false
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = onError(utteranceId, TextToSpeech.ERROR)

        override fun onError(utteranceId: String?, errorCode: Int) {
            val active = finalUtteranceId ?: return
            if (utteranceId != null && utteranceId.substringBefore('-') == active.substringBefore('-')) {
                finalUtteranceId = null
                _isSpeaking.value = false
            }
        }
    }
}
