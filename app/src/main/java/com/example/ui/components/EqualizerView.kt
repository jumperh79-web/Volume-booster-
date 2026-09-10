package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EqualizerPreset
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EqualizerView(
    selectedPreset: EqualizerPreset,
    bandLevels: List<Int>, // 5 band levels (-12 to +12 dB)
    isEnabled: Boolean,
    onPresetSelect: (EqualizerPreset) -> Unit,
    onBandChange: (bandIndex: Int, levelDb: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp)
            .testTag("equalizer_view_container")
    ) {
        Text(
            text = "5-BAND EQUALIZER",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EqualizerPreset.ALL_PRESETS.forEach { preset ->
                val isSelected = selectedPreset.name == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = { if (isEnabled) onPresetSelect(preset) },
                    enabled = isEnabled,
                    label = {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = CyberSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("preset_chip_${preset.name.lowercase().replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5 Bands Column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bandLevels.forEachIndexed { index, level ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
                ) {
                    // dB level indicator
                    Text(
                        text = if (level > 0) "+${level}dB" else "${level}dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (level != 0 && isEnabled) NeonCyan else TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom Slider for band gain (-12 to +12 dB)
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .width(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background guide line
                        Box(
                            modifier = Modifier
                                .height(130.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyberBorder)
                        )

                        // Slider
                        Slider(
                            value = level.toFloat(),
                            onValueChange = { newValue ->
                                if (isEnabled) onBandChange(index, newValue.toInt())
                            },
                            valueRange = -12f..12f,
                            steps = 23, // 1 dB step increments
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isEnabled) NeonCyan else TextMuted,
                                activeTrackColor = if (isEnabled) NeonCyan else TextMuted,
                                inactiveTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("eq_band_slider_$index")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Frequency label
                    Text(
                        text = bandFrequencies.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
