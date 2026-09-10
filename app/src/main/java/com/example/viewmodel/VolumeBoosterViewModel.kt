package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioFxController
import com.example.audio.AudioTester
import com.example.audio.EqualizerPreset
import com.example.service.VolumeBoosterService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VolumeBoosterUiState(
    val isBoosterEnabled: Boolean = true,
    val boostPercentage: Int = 100,
    val maxBoostLimit: Int = 200,
    val bassBoostStrength: Int = 50,
    val virtualizerStrength: Int = 30,
    val selectedPreset: EqualizerPreset = EqualizerPreset.FLAT,
    val eqBandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // -12 to +12 dB
    val isMuted: Boolean = false,
    val previousBoostPercentage: Int = 100,
    val mediaVolume: Int = 0,
    val maxMediaVolume: Int = 15,
    val ringVolume: Int = 0,
    val maxRingVolume: Int = 15,
    val alarmVolume: Int = 0,
    val maxAlarmVolume: Int = 15,
    val callVolume: Int = 0,
    val maxCallVolume: Int = 15,
    val isTestAudioPlaying: Boolean = false,
    val showWarningDialog: Boolean = false,
    val pendingBoostLevel: Int = 0,
    val selectedTab: Int = 0
)

class VolumeBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("volume_booster_prefs", Context.MODE_PRIVATE)
    private val audioFxController = AudioFxController(application)
    private val audioTester = AudioTester()

    private val _uiState = MutableStateFlow(VolumeBoosterUiState())
    val uiState: StateFlow<VolumeBoosterUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
        updateStreamVolumes()
        applyAllEffects()
        startVolumeObserver()
    }

    private fun loadPreferences() {
        val enabled = prefs.getBoolean("is_enabled", true)
        val boostPct = prefs.getInt("boost_pct", 100)
        val maxLimit = prefs.getInt("max_limit", 200)
        val bassBoost = prefs.getInt("bass_boost", 50)
        val virtualizer = prefs.getInt("virtualizer", 30)
        val presetName = prefs.getString("preset_name", EqualizerPreset.FLAT.name) ?: EqualizerPreset.FLAT.name

        val foundPreset = EqualizerPreset.ALL_PRESETS.find { it.name == presetName } ?: EqualizerPreset.FLAT
        val bandLevels = foundPreset.bandLevelsDb

        _uiState.update {
            it.copy(
                isBoosterEnabled = enabled,
                boostPercentage = boostPct,
                maxBoostLimit = maxLimit,
                bassBoostStrength = bassBoost,
                virtualizerStrength = virtualizer,
                selectedPreset = foundPreset,
                eqBandLevels = bandLevels
            )
        }
    }

    private fun savePreferences() {
        val currentState = _uiState.value
        prefs.edit()
            .putBoolean("is_enabled", currentState.isBoosterEnabled)
            .putInt("boost_pct", currentState.boostPercentage)
            .putInt("max_limit", currentState.maxBoostLimit)
            .putInt("bass_boost", currentState.bassBoostStrength)
            .putInt("virtualizer", currentState.virtualizerStrength)
            .putString("preset_name", currentState.selectedPreset.name)
            .apply()
    }

    private fun startVolumeObserver() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updateStreamVolumes()
            }
        }
    }

    fun updateStreamVolumes() {
        val media = audioFxController.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxMedia = audioFxController.getMaxStreamVolume(AudioManager.STREAM_MUSIC)
        val ring = audioFxController.getStreamVolume(AudioManager.STREAM_RING)
        val maxRing = audioFxController.getMaxStreamVolume(AudioManager.STREAM_RING)
        val alarm = audioFxController.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxAlarm = audioFxController.getMaxStreamVolume(AudioManager.STREAM_ALARM)
        val call = audioFxController.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        val maxCall = audioFxController.getMaxStreamVolume(AudioManager.STREAM_VOICE_CALL)

        _uiState.update {
            it.copy(
                mediaVolume = media,
                maxMediaVolume = maxMedia,
                ringVolume = ring,
                maxRingVolume = maxRing,
                alarmVolume = alarm,
                maxAlarmVolume = maxAlarm,
                callVolume = call,
                maxCallVolume = maxCall
            )
        }
    }

    fun setBoosterEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isBoosterEnabled = enabled) }
        applyAllEffects()
        savePreferences()
        updateServiceState()
    }

    fun setBoostPercentage(pct: Int, overrideWarning: Boolean = false) {
        val currentState = _uiState.value
        val clamped = pct.coerceIn(0, currentState.maxBoostLimit)

        // Show hearing safety warning if boosting > 120% and not previously overridden
        if (clamped > 120 && !overrideWarning && clamped > currentState.boostPercentage) {
            _uiState.update {
                it.copy(
                    showWarningDialog = true,
                    pendingBoostLevel = clamped
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                boostPercentage = clamped,
                isMuted = clamped == 0,
                showWarningDialog = false
            )
        }
        applyAllEffects()
        savePreferences()
        updateServiceState()
    }

    fun dismissWarningDialog() {
        _uiState.update { it.copy(showWarningDialog = false) }
    }

    fun confirmHighBoostWarning() {
        val pending = _uiState.value.pendingBoostLevel
        setBoostPercentage(pending, overrideWarning = true)
    }

    fun setMaxBoostLimit(limit: Int) {
        _uiState.update { state ->
            val newBoost = state.boostPercentage.coerceAtMost(limit)
            state.copy(
                maxBoostLimit = limit,
                boostPercentage = newBoost
            )
        }
        applyAllEffects()
        savePreferences()
    }

    fun toggleMute() {
        _uiState.update { state ->
            if (state.isMuted) {
                val restore = if (state.previousBoostPercentage == 0) 100 else state.previousBoostPercentage
                state.copy(isMuted = false, boostPercentage = restore)
            } else {
                state.copy(isMuted = true, previousBoostPercentage = state.boostPercentage, boostPercentage = 0)
            }
        }
        applyAllEffects()
    }

    fun setBassBoostStrength(strengthPct: Int) {
        _uiState.update { it.copy(bassBoostStrength = strengthPct.coerceIn(0, 100)) }
        applyAllEffects()
        savePreferences()
    }

    fun setVirtualizerStrength(strengthPct: Int) {
        _uiState.update { it.copy(virtualizerStrength = strengthPct.coerceIn(0, 100)) }
        applyAllEffects()
        savePreferences()
    }

    fun selectPreset(preset: EqualizerPreset) {
        _uiState.update {
            it.copy(
                selectedPreset = preset,
                eqBandLevels = preset.bandLevelsDb
            )
        }
        applyAllEffects()
        savePreferences()
    }

    fun setEqBandLevel(bandIndex: Int, levelDb: Int) {
        _uiState.update { state ->
            val updatedBands = state.eqBandLevels.toMutableList()
            if (bandIndex in updatedBands.indices) {
                updatedBands[bandIndex] = levelDb.coerceIn(-12, 12)
            }
            val customPreset = EqualizerPreset("Custom", updatedBands)
            state.copy(
                selectedPreset = customPreset,
                eqBandLevels = updatedBands
            )
        }
        applyAllEffects()
        savePreferences()
    }

    fun setStreamVolume(streamType: Int, volume: Int) {
        audioFxController.setStreamVolume(streamType, volume)
        updateStreamVolumes()
    }

    fun toggleTestAudio() {
        val isCurrentlyPlaying = _uiState.value.isTestAudioPlaying
        if (isCurrentlyPlaying) {
            audioTester.stopTesting()
            _uiState.update { it.copy(isTestAudioPlaying = false) }
        } else {
            audioTester.startTesting { sessionId ->
                audioFxController.initAudioEffects(sessionId)
                applyAllEffects()
            }
            _uiState.update { it.copy(isTestAudioPlaying = true) }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    private fun applyAllEffects() {
        val state = _uiState.value
        audioFxController.setBoostPercentage(state.boostPercentage, state.isBoosterEnabled)
        audioFxController.setBassBoost(state.bassBoostStrength, state.isBoosterEnabled)
        audioFxController.setVirtualizer(state.virtualizerStrength, state.isBoosterEnabled)
        audioFxController.setEqualizerBands(state.eqBandLevels, state.isBoosterEnabled)
    }

    private fun updateServiceState() {
        val state = _uiState.value
        if (state.isBoosterEnabled) {
            VolumeBoosterService.startService(getApplication(), state.boostPercentage, true)
        } else {
            VolumeBoosterService.stopService(getApplication())
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioTester.stopTesting()
        audioFxController.releaseEffects()
    }
}
