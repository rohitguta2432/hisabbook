package com.hisabbook.app.data.backup

import android.content.Context
import android.net.Uri
import com.hisabbook.app.data.repo.HisabBookRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val repo: HisabBookRepository
) {

    companion object {
        private const val MAGIC = "HBBK1"
        private const val ITER = 120_000
        private const val KEY_BITS = 256
        private const val SALT_BYTES = 16
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }

    /** Export all data → encrypted JSON bytes using PBKDF2(passphrase) → AES/GCM. */
    suspend fun exportTo(uri: Uri, passphrase: CharArray) = withContext(Dispatchers.IO) {
        val persons = repo.observePersons().first()
        val entries = repo.observeAllEntries().first()
        val json = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("persons", JSONArray().apply {
                persons.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("name", it.name)
                        put("phone", it.phone)
                        put("type", it.type.name)
                        put("balancePaise", it.balancePaise)
                    })
                }
            })
            put("entries", JSONArray().apply {
                entries.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("type", it.type.name)
                        put("personId", it.personId)
                        put("personName", it.personName)
                        put("amountPaise", it.amountPaise)
                        put("item", it.item)
                        put("note", it.note)
                        put("langCode", it.langCode)
                        put("createdAt", it.createdAt)
                        put("source", it.source.name)
                    })
                }
            })
        }
        val plain = json.toString().toByteArray(Charsets.UTF_8)
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val ct = cipher.doFinal(plain)

        val out = ByteArrayOutputStream()
        out.write(MAGIC.toByteArray(Charsets.US_ASCII))
        out.write(salt); out.write(iv); out.write(ct)

        ctx.contentResolver.openOutputStream(uri)?.use { it.write(out.toByteArray()) }
            ?: error("Cannot write to $uri")
    }

    /** Import: decrypt + replace DB contents with file contents. */
    suspend fun importFrom(uri: Uri, passphrase: CharArray): Int = withContext(Dispatchers.IO) {
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Cannot read $uri")
        require(bytes.size >= MAGIC.length + SALT_BYTES + IV_BYTES + 16) { "Backup file too small" }
        val magic = String(bytes, 0, MAGIC.length, Charsets.US_ASCII)
        require(magic == MAGIC) { "Not a HisabBook backup file" }
        val salt = bytes.copyOfRange(MAGIC.length, MAGIC.length + SALT_BYTES)
        val iv = bytes.copyOfRange(MAGIC.length + SALT_BYTES, MAGIC.length + SALT_BYTES + IV_BYTES)
        val ct = bytes.copyOfRange(MAGIC.length + SALT_BYTES + IV_BYTES, bytes.size)

        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val plain = cipher.doFinal(ct)
        val json = JSONObject(String(plain, Charsets.UTF_8))

        val persons = json.getJSONArray("persons")
        val entries = json.getJSONArray("entries")
        var count = 0

        for (i in 0 until persons.length()) {
            val p = persons.getJSONObject(i)
            repo.upsertPerson(
                com.hisabbook.app.data.model.Person(
                    id = p.getString("id"),
                    name = p.getString("name"),
                    phone = p.optString("phone").takeIf { it.isNotEmpty() && it != "null" },
                    type = runCatching { com.hisabbook.app.data.model.PersonType.valueOf(p.getString("type")) }
                        .getOrDefault(com.hisabbook.app.data.model.PersonType.CUSTOMER),
                    balancePaise = p.optLong("balancePaise", 0L)
                )
            )
        }
        for (i in 0 until entries.length()) {
            val e = entries.getJSONObject(i)
            repo.saveEntry(
                com.hisabbook.app.data.model.Entry(
                    id = e.getString("id"),
                    type = runCatching { com.hisabbook.app.data.model.EntryType.valueOf(e.getString("type")) }
                        .getOrDefault(com.hisabbook.app.data.model.EntryType.KHARCH),
                    personId = e.optString("personId").takeIf { it.isNotEmpty() && it != "null" },
                    personName = e.optString("personName").takeIf { it.isNotEmpty() && it != "null" },
                    amountPaise = e.getLong("amountPaise"),
                    item = e.optString("item"),
                    note = e.optString("note").takeIf { it.isNotEmpty() && it != "null" },
                    langCode = e.optString("langCode", "hi"),
                    createdAt = e.optLong("createdAt", System.currentTimeMillis()),
                    source = runCatching { com.hisabbook.app.data.model.Source.valueOf(e.getString("source")) }
                        .getOrDefault(com.hisabbook.app.data.model.Source.MANUAL)
                )
            )
            count++
        }
        count
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(passphrase, salt, ITER, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val raw = factory.generateSecret(spec).encoded
        return SecretKeySpec(raw, "AES")
    }
}
