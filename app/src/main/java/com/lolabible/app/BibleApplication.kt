package com.lolabible.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.lolabible.app.data.BibleRepository
import com.lolabible.app.data.SettingsRepository

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class BibleApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** App-wide singletons, created once per process (manual dependency injection). */
class AppContainer(context: Context) {
    val bibleRepository = BibleRepository(assets = context.assets::open)
    val settingsRepository = SettingsRepository(context.settingsDataStore)
}
