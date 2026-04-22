package com.hisabbook.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hisabbook_prefs")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val store = ctx.dataStore

    val onboardingDone: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING] ?: false }
    val lockEnabled: Flow<Boolean> = store.data.map { it[KEY_LOCK] ?: true }

    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[KEY_ONBOARDING] = done }
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        store.edit { it[KEY_LOCK] = enabled }
    }

    companion object {
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val KEY_LOCK = booleanPreferencesKey("lock_enabled")
    }
}
