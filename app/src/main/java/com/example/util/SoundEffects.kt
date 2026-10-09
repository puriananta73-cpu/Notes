package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object SoundEffects {
    suspend fun playRadioChirp() = withContext(Dispatchers.Default) {
        try {
            val sampleRate = 22050
            val durationMs = 120
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val freq1 = 1760.0 // A6
            val freq2 = 2349.0 // D7

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val freq = if (i < numSamples / 2) freq1 else freq2
                val sample = sin(2.0 * Math.PI * freq * t) * 0.3 * (1.0 - i.toDouble() / numSamples)
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            kotlinx.coroutines.delay(durationMs.toLong() + 50)
            track.release()
        } catch (_: Exception) {}
    }

    suspend fun playCallConnectedTone() = withContext(Dispatchers.Default) {
        try {
            val sampleRate = 22050
            val durationMs = 250
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val sample = (sin(2.0 * Math.PI * 880.0 * t) + sin(2.0 * Math.PI * 1200.0 * t)) * 0.2
                buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            kotlinx.coroutines.delay(durationMs.toLong() + 50)
            track.release()
        } catch (_: Exception) {}
    }
}
