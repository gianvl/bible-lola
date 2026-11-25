package com.lolabible.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.lolabible.app.BibleApplication
import com.lolabible.app.data.BibleRepository
import com.lolabible.app.data.Books
import com.lolabible.app.data.ChapterRef
import com.lolabible.app.data.SettingsRepository
import com.lolabible.app.data.TextSize
import com.lolabible.app.data.Translation
import com.lolabible.app.speech.Speaker
import com.lolabible.app.ui.ReaderRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReaderUiState(
    val ref: ChapterRef,
    val translation: Translation,
    val textSize: Float,
    val verses: List<String>,
    val isSpeaking: Boolean,
) {
    val title: String get() = "${Books.get(ref.book).name(translation)} ${ref.chapter}"
    val hasPrevious: Boolean get() = ref.previous() != null
    val hasNext: Boolean get() = ref.next() != null
    val canShrinkText: Boolean get() = textSize > TextSize.MIN
    val canGrowText: Boolean get() = textSize < TextSize.MAX
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val bibleRepository: BibleRepository,
    private val settingsRepository: SettingsRepository,
    private val speaker: Speaker,
) : ViewModel() {

    // The current chapter lives in SavedStateHandle (as one key, so book and chapter always
    // change together) and survives process death even after Lola has paged ahead.
    private val position = savedStateHandle
        .getStateFlow(KEY_POSITION, savedStateHandle.toRoute<ReaderRoute>().let { encode(ChapterRef(it.book, it.chapter)) })
    private val ref = position.map(::decode)

    private val settings = settingsRepository.settings

    /** What is being shown: changes when the chapter or the translation changes. */
    private val passage = combine(ref, settings.map { it.translation }) { r, t -> r to t }
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val content = passage.mapLatest { (r, t) -> Triple(r, t, bibleRepository.verses(t, r)) }

    val uiState: StateFlow<ReaderUiState?> =
        combine(content, settings, speaker.isSpeaking) { (r, t, verses), s, speaking ->
            ReaderUiState(ref = r, translation = t, textSize = s.textSize, verses = verses, isSpeaking = speaking)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val speechEvents: SharedFlow<Speaker.Event> = speaker.events

    init {
        passage.onEach { (r, _) ->
            // Never keep reading aloud a chapter that is no longer on screen.
            speaker.stop()
            settingsRepository.setLastRead(r)
        }.launchIn(viewModelScope)
    }

    fun goToNext() {
        decode(position.value).next()?.let(::goTo)
    }

    fun goToPrevious() {
        decode(position.value).previous()?.let(::goTo)
    }

    fun makeTextSmaller() = changeTextSize(-TextSize.STEP)

    fun makeTextLarger() = changeTextSize(TextSize.STEP)

    fun toggleReadAloud() {
        val state = uiState.value ?: return
        if (state.isSpeaking) {
            speaker.stop()
            return
        }
        val heading = "${Books.get(state.ref.book).name(state.translation)}, " +
            "${state.translation.labels.chapter} ${state.ref.chapter}."
        speaker.speak(listOf(heading) + state.verses, state.translation.speechLocales)
    }

    private fun goTo(ref: ChapterRef) {
        savedStateHandle[KEY_POSITION] = encode(ref)
    }

    private fun changeTextSize(delta: Float) {
        viewModelScope.launch { settingsRepository.changeTextSize(delta) }
    }

    override fun onCleared() {
        speaker.shutdown()
    }

    companion object {
        private const val KEY_POSITION = "position"

        private fun encode(ref: ChapterRef): Int = ref.book * 1000 + ref.chapter
        private fun decode(value: Int): ChapterRef = ChapterRef(value / 1000, value % 1000)

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BibleApplication
                ReaderViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    bibleRepository = app.container.bibleRepository,
                    settingsRepository = app.container.settingsRepository,
                    speaker = Speaker(app),
                )
            }
        }
    }
}
