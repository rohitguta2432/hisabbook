package com.hisabbook.app.data.stt

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer

/** Real Vosk wrapper. Requires model at `filesDir/vosk/hi-small/` copied from assets or PAD.
 *  Model source: https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip (~42 MB). */
@Singleton
class VoskSttEngine @Inject constructor(@ApplicationContext private val ctx: Context) : SttEngine {

    private val modelDir: File get() = File(ctx.filesDir, "vosk/hi-small")
    private var cachedModel: Model? = null

    override suspend fun isReady(): Boolean =
        modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true

    override suspend fun transcribe(audio: Flow<ByteArray>, langCode: String): String {
        if (!isReady()) {
            // Drain audio so recorder stops cleanly, return empty so VM hits Error state.
            audio.collect { /* drop */ }
            return ""
        }
        val model = cachedModel ?: runCatching { Model(modelDir.absolutePath) }
            .onSuccess { cachedModel = it }
            .getOrElse { Log.e(TAG, "Vosk model load failed", it); return "" }

        val recognizer = Recognizer(model, 16_000f)
        try {
            audio.collect { chunk ->
                recognizer.acceptWaveForm(chunk, chunk.size)
            }
            val finalJson = recognizer.finalResult
            return runCatching { JSONObject(finalJson).optString("text") }.getOrDefault("")
        } finally {
            runCatching { recognizer.close() }
        }
    }

    companion object {
        private const val TAG = "VoskSttEngine"
    }
}
