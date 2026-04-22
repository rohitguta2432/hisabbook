package com.hisabbook.app.data.stt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Stub Vosk wrapper.
 *  Real integration: add `com.alphacephei:vosk-android:0.3.47` dep, bundle
 *  `vosk-model-small-hi-0.22` under app/src/main/assets/models/hi-small/, copy to
 *  files dir on first run, and feed PCM bytes to `Recognizer.acceptWaveForm()`.
 *
 *  Currently returns a canned demo transcript so UI flow is testable end-to-end. */
@Singleton
class VoskSttEngine @Inject constructor(@ApplicationContext private val ctx: Context) : SttEngine {

    private val modelDir: File get() = File(ctx.filesDir, "vosk/hi-small")

    override suspend fun isReady(): Boolean {
        // Not ready until real asset copy + vosk-android dep are in place.
        return modelDir.exists() && modelDir.listFiles()?.isNotEmpty() == true
    }

    override suspend fun transcribe(audio: Flow<ByteArray>, langCode: String): String {
        // Drain the audio flow so AudioRecord gets a chance to run and stop cleanly.
        var totalBytes = 0
        audio.collect { totalBytes += it.size }
        // Demo transcript — real Vosk output goes here when model is wired.
        return "Ramesh ko do sau ka doodh udhar diya"
    }
}
