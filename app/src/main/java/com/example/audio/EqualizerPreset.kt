package com.example.audio

data class EqualizerPreset(
    val name: String,
    val bandLevelsDb: List<Int> // 5 bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz in dB (-12 to +12)
) {
    companion object {
        val FLAT = EqualizerPreset("Flat", listOf(0, 0, 0, 0, 0))
        val BASS_BOOST = EqualizerPreset("Bass Boost", listOf(8, 6, 2, 0, -1))
        val TREBLE_BOOST = EqualizerPreset("Treble Boost", listOf(-2, 0, 3, 7, 9))
        val ROCK = EqualizerPreset("Rock", listOf(5, 3, -1, 3, 6))
        val POP = EqualizerPreset("Pop", listOf(-1, 2, 5, 3, -1))
        val JAZZ = EqualizerPreset("Jazz", listOf(4, 2, 0, 2, 5))
        val EDM = EqualizerPreset("EDM", listOf(7, 4, -2, 4, 7))
        val VOCAL = EqualizerPreset("Vocal Clear", listOf(-3, 1, 6, 4, 1))

        val ALL_PRESETS = listOf(
            FLAT, BASS_BOOST, TREBLE_BOOST, ROCK, POP, JAZZ, EDM, VOCAL
        )
    }
}
