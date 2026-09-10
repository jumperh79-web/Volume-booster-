package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioFxController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var audioSessionId: Int = 0

    init {
        initAudioEffects(audioSessionId)
    }

    fun initAudioEffects(sessionId: Int = 0) {
        audioSessionId = sessionId
        releaseEffects()

        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "LoudnessEnhancer init failed: ${e.message}")
        }

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Equalizer init failed: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "BassBoost init failed: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Virtualizer init failed: ${e.message}")
        }
    }

    /**
     * Sets volume boost level in percentage (0% to 200%).
     * 0% = 0 mB (no boost)
     * 100% = 1000 mB (+10 dB)
     * 200% = 2500 mB (+25 dB)
     */
    fun setBoostPercentage(boostPct: Int, isEnabled: Boolean) {
        try {
            loudnessEnhancer?.let { enhancer ->
                if (!isEnabled || boostPct <= 0) {
                    enhancer.setTargetGain(0)
                    enhancer.enabled = false
                } else {
                    enhancer.enabled = true
                    // Map 0..200% to 0..2500 mB (millibels)
                    val targetGainMb = (boostPct * 12.5f).toInt().coerceIn(0, 3000)
                    enhancer.setTargetGain(targetGainMb)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error setting gain: ${e.message}")
        }
    }

    /**
     * Sets Bass Boost level (0 to 100%). Maps to 0..1000 strength.
     */
    fun setBassBoost(strengthPct: Int, isEnabled: Boolean) {
        try {
            bassBoost?.let { bb ->
                if (!isEnabled || strengthPct <= 0) {
                    bb.enabled = false
                } else {
                    bb.enabled = true
                    if (bb.strengthSupported) {
                        val strength = (strengthPct * 10).coerceIn(0, 1000).toShort()
                        bb.setStrength(strength)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error setting bass boost: ${e.message}")
        }
    }

    /**
     * Sets 3D Surround Virtualizer level (0 to 100%). Maps to 0..1000 strength.
     */
    fun setVirtualizer(strengthPct: Int, isEnabled: Boolean) {
        try {
            virtualizer?.let { virt ->
                if (!isEnabled || strengthPct <= 0) {
                    virt.enabled = false
                } else {
                    virt.enabled = true
                    if (virt.strengthSupported) {
                        val strength = (strengthPct * 10).coerceIn(0, 1000).toShort()
                        virt.setStrength(strength)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error setting virtualizer: ${e.message}")
        }
    }

    /**
     * Sets band levels for 5 equalizer bands in dB (-12 dB to +12 dB).
     */
    fun setEqualizerBands(bandLevelsDb: List<Int>, isEnabled: Boolean) {
        try {
            equalizer?.let { eq ->
                eq.enabled = isEnabled
                if (!isEnabled) return

                val numberOfBands = eq.numberOfBands.toInt()
                val minLevel = eq.bandLevelRange?.get(0) ?: -1500
                val maxLevel = eq.bandLevelRange?.get(1) ?: 1500

                for (i in 0 until minOf(numberOfBands, bandLevelsDb.size)) {
                    val db = bandLevelsDb[i]
                    // Convert dB (-12..+12) to millibels (-1200..+1200)
                    val mB = (db * 100).coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    eq.setBandLevel(i.toShort(), mB)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error setting equalizer bands: ${e.message}")
        }
    }

    // AudioManager Stream Controls
    fun getStreamVolume(streamType: Int): Int {
        return try {
            audioManager.getStreamVolume(streamType)
        } catch (e: Exception) {
            0
        }
    }

    fun getMaxStreamVolume(streamType: Int): Int {
        return try {
            audioManager.getStreamMaxVolume(streamType)
        } catch (e: Exception) {
            15
        }
    }

    fun setStreamVolume(streamType: Int, volume: Int) {
        try {
            audioManager.setStreamVolume(streamType, volume.coerceIn(0, getMaxStreamVolume(streamType)), 0)
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error setting stream volume: ${e.message}")
        }
    }

    fun releaseEffects() {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            virtualizer?.release()
            virtualizer = null
        } catch (e: Exception) {
            Log.e("AudioFxController", "Error releasing effects: ${e.message}")
        }
    }
}
