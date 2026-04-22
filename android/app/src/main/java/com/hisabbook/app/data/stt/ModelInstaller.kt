package com.hisabbook.app.data.stt

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Unpacks Vosk Hindi model from APK assets to filesDir on first use.
 *  Expected asset path: `assets/models/vosk-model-small-hi-0.22.zip`
 *  Unpacks to: `filesDir/vosk/hi-small/` so VoskSttEngine can load it. */
@Singleton
class ModelInstaller @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val targetDir = File(ctx.filesDir, "vosk/hi-small")

    fun isInstalled(): Boolean =
        targetDir.exists() &&
            File(targetDir, "am").exists() && // Vosk models contain `am/`, `graph/`, etc.
            (targetDir.listFiles()?.size ?: 0) > 2

    fun assetAvailable(): Boolean = runCatching {
        ctx.assets.list("models")?.any { it.endsWith(".zip") } == true
    }.getOrDefault(false)

    suspend fun install(): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInstalled()) return@withContext Result.success(Unit)
        val zipName = runCatching {
            ctx.assets.list("models")?.firstOrNull { it.endsWith(".zip") }
        }.getOrNull() ?: return@withContext Result.failure(
            IllegalStateException("No Vosk model zip in app/src/main/assets/models/")
        )
        runCatching {
            targetDir.mkdirs()
            ctx.assets.open("models/$zipName").use { input ->
                ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val stripped = entry.name.substringAfter('/', missingDelimiterValue = entry.name)
                        val outFile = File(targetDir, stripped)
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { out ->
                                val buf = ByteArray(8192)
                                var n: Int
                                while (zis.read(buf).also { n = it } > 0) out.write(buf, 0, n)
                            }
                        }
                        entry = zis.nextEntry
                    }
                }
            }
            Log.i(TAG, "Vosk model installed to ${targetDir.absolutePath}")
            Unit
        }.onFailure { Log.e(TAG, "Model install failed", it) }
    }

    fun uninstall() {
        targetDir.deleteRecursively()
    }

    companion object { private const val TAG = "ModelInstaller" }
}
