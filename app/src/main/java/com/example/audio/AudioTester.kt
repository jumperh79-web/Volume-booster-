package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.concurrent.thread
import kotlin.math.sin

class AudioTester {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var playbackThread: Thread? = null

    val audioSessionId: Int
        get() = audioTrack?.audioSessionId ?: 0

    fun startTesting(onSessionCreated: ((Int) -> Unit)? = null) {
        if (isPlaying) return

        try {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .setEncoding(audioFormat)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isPlaying = true

            val sessionId = audioTrack?.audioSessionId ?: 0
            onSessionCreated?.invoke(sessionId)

            playbackThread = thread(start = true, isDaemon = true) {
                generateRhythmicAudio(sampleRate, bufferSize)
            }
        } catch (e: Exception) {
            Log.e("AudioTester", "Failed to start audio tester: ${e.message}")
            isPlaying = false
        }
    }

    private fun generateRhythmicAudio(sampleRate: Int, bufferSize: Int) {
        val numSamples = bufferSize / 2
        val samples = ShortArray(numSamples)
        var sampleIndex = 0L

        // Dual-tone rhythmic synth pulse with bass pulse
        while (isPlaying) {
            val seconds = sampleIndex.toDouble() / sampleRate
            
            // Rhythm beat every 0.5s
            val beatPhase = (seconds % 0.5) / 0.5
            val beatEnv = Math.exp(-6.0 * beatPhase)

            // Bass frequency 65Hz (C2) + Lead frequency 440Hz (A4)
            val bassVal = sin(2.0 * Math.PI * 65.0 * seconds) * 0.7
            val leadVal = sin(2.0 * Math.PI * 440.0 * seconds) * 0.3
            val synthVal = (bassVal + leadVal) * beatEnv

            val sampleVal = (synthVal * Short.MAX_VALUE * 0.5).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            for (i in 0 until numSamples step 2) {
                samples[i] = sampleVal       // Left channel
                samples[i + 1] = sampleVal   // Right channel
                sampleIndex++
            }

            audioTrack?.write(samples, 0, numSamples)
        }
    }

    fun stopTesting() {
        isPlaying = false
        try {
            playbackThread?.interrupt()
            playbackThread = null
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e("AudioTester", "Error stopping audio tester: ${e.message}")
        }
    }

    fun isPlaying(): Boolean = isPlaying
}
