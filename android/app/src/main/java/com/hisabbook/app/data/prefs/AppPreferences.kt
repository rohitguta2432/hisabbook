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
    val darkMode: Flow<Boolean?> = store.data.map { it[KEY_DARK_MODE] }
    val langCode: Flow<String> = store.data.map { it[KEY_LANG] ?: "hi" }
    val cachedTier: Flow<com.hisabbook.app.domain.device.VoiceTier?> = store.data.map { prefs ->
        prefs[KEY_TIER]?.let { runCatching { com.hisabbook.app.domain.device.VoiceTier.valueOf(it) }.getOrNull() }
    }

    suspend fun setCachedTier(tier: com.hisabbook.app.domain.device.VoiceTier) {
        store.edit { it[KEY_TIER] = tier.name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[KEY_ONBOARDING] = done }
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        store.edit { it[KEY_LOCK] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        store.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setLangCode(lang: String) {
        store.edit { it[KEY_LANG] = lang }
    }

    companion object {
        private val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val KEY_LOCK = booleanPreferencesKey("lock_enabled")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_LANG = androidx.datastore.preferences.core.stringPreferencesKey("lang_code")
        private val KEY_TIER = androidx.datastore.preferences.core.stringPreferencesKey("voice_tier")
    }
}
