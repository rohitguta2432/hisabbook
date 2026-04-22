package com.hisabbook.app.data.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

/** 16 kHz PCM mono AudioRecord wrapper producing a Flow of ByteArray chunks. */
@Singleton
class AudioRecorder @Inject constructor(@ApplicationContext private val ctx: Context) {

    companion object {
        const val SAMPLE_RATE = 16_000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun record(): Flow<ByteArray> = channelFlow {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING).coerceAtLeast(4096)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferSize * 2
        )
        try {
            recorder.startRecording()
            val buf = ByteArray(bufferSize)
            while (!isClosedForSend) {
                val read = recorder.read(buf, 0, buf.size)
                if (read <= 0) continue
                val chunk = buf.copyOf(read)
                trySend(chunk)
            }
        } finally {
            runCatching { recorder.stop() }
            runCatching { recorder.release() }
        }
    }.flowOn(Dispatchers.IO)
}
