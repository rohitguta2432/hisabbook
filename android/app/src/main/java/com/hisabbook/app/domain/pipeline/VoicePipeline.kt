package com.hisabbook.app.domain.pipeline

import com.hisabbook.app.data.audio.AudioRecorder
import com.hisabbook.app.data.stt.SttEngine
import com.hisabbook.app.domain.parser.ParsedEntry
import com.hisabbook.app.domain.parser.RulesParser
import javax.inject.Inject
import javax.inject.Singleton

data class VoiceResult(
    val transcript: String,
    val parsed: ParsedEntry
)

/** Voice pipeline: AudioRecorder → SttEngine → RulesParser.
 *  T0 tier. T1-T3 wrappers can wrap this or replace the STT engine. */
@Singleton
class VoicePipeline @Inject constructor(
    private val recorder: AudioRecorder,
    private val stt: SttEngine
) {
    /** Record until [stop] is called externally (by disposing Flow from caller),
     *  then run STT + parser. Returns (transcript, parsed entry). */
    suspend fun recordAndParse(langCode: String = "hi"): VoiceResult {
        val audio = recorder.record()
        val transcript = stt.transcribe(audio, langCode)
        val parsed = RulesParser.parse(transcript)
        return VoiceResult(transcript, parsed)
    }

    fun hasMicPermission() = recorder.hasPermission()
}
