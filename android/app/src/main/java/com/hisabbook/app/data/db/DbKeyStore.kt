package com.hisabbook.app.data.db

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbKeyStore @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val master = MasterKey.Builder(ctx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            ctx,
            "hisabbook_keys",
            master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOrCreate(): ByteArray {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) return android.util.Base64.decode(existing, android.util.Base64.NO_WRAP)
        val fresh = HisabBookDb.generatePassphrase()
        prefs.edit().putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(fresh, android.util.Base64.NO_WRAP)).apply()
        return fresh
    }

    companion object {
        private const val KEY_DB_PASSPHRASE = "db_passphrase_b64"
    }
}
