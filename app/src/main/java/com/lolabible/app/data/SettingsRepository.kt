package com.lolabible.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class Settings(
    val translation: Translation = Translation.KJV,
    val textSize: Float = TextSize.DEFAULT,
    val lastRead: ChapterRef? = null,
)

object TextSize {
    const val MIN = 16f
    const val MAX = 40f
    const val DEFAULT = 22f
    const val STEP = 2f
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<Settings> = dataStore.data
        .map { prefs ->
            Settings(
                translation = prefs[TRANSLATION]
                    ?.let { name -> Translation.entries.firstOrNull { it.name == name } }
                    ?: Translation.KJV,
                textSize = (prefs[TEXT_SIZE] ?: TextSize.DEFAULT).coerceIn(TextSize.MIN, TextSize.MAX),
                lastRead = readLastRead(prefs),
            )
        }
        .distinctUntilChanged()

    suspend fun setTranslation(translation: Translation) {
        dataStore.edit { it[TRANSLATION] = translation.name }
    }

    suspend fun changeTextSize(delta: Float) {
        dataStore.edit { prefs ->
            val current = prefs[TEXT_SIZE] ?: TextSize.DEFAULT
            prefs[TEXT_SIZE] = (current + delta).coerceIn(TextSize.MIN, TextSize.MAX)
        }
    }

    suspend fun setLastRead(ref: ChapterRef) {
        dataStore.edit {
            it[LAST_BOOK] = ref.book
            it[LAST_CHAPTER] = ref.chapter
        }
    }

    private fun readLastRead(prefs: Preferences): ChapterRef? {
        val book = prefs[LAST_BOOK] ?: return null
        val chapter = prefs[LAST_CHAPTER] ?: return null
        // Ignore anything stale or corrupt rather than crash on launch.
        return runCatching { ChapterRef(book, chapter) }.getOrNull()
    }

    private companion object {
        val TRANSLATION = stringPreferencesKey("translation")
        val TEXT_SIZE = floatPreferencesKey("text_size")
        val LAST_BOOK = intPreferencesKey("last_book")
        val LAST_CHAPTER = intPreferencesKey("last_chapter")
    }
}
