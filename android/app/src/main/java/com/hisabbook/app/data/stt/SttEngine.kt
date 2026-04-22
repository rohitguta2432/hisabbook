package com.hisabbook.app.data.stt

import kotlinx.coroutines.flow.Flow

/** Common STT interface. Implementations: Vosk (offline), Android SpeechRecognizer (fallback). */
interface SttEngine {
    /** Returns best-guess final transcript for a stream of PCM 16kHz mono audio chunks. */
    suspend fun transcribe(audio: Flow<ByteArray>, langCode: String = "hi"): String

    /** Whether engine can run on this device (asset present, init ok). */
    suspend fun isReady(): Boolean
}
