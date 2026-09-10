package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.EqualizerView
import com.example.ui.components.RotaryKnob
import com.example.ui.components.SafetyWarningDialog
import com.example.ui.components.StreamVolumesView
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.VolumeBoosterViewModel

@Composable
fun MainVolumeBoosterScreen(
    viewModel: VolumeBoosterViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = CyberObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            HeaderSection(
                isBoosterEnabled = state.isBoosterEnabled,
                onToggleBooster = { viewModel.setBoosterEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Hero Rotary Knob
            RotaryKnob(
                value = state.boostPercentage,
                maxLimit = state.maxBoostLimit,
                isEnabled = state.isBoosterEnabled,
                onValueChange = { viewModel.setBoostPercentage(it) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Quick Boost Buttons Row
            QuickBoostRow(
                currentBoost = state.boostPercentage,
                maxLimit = state.maxBoostLimit,
                isEnabled = state.isBoosterEnabled,
                onBoostSelected = { viewModel.setBoostPercentage(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Audio Spectrum Visualizer
            AudioVisualizer(
                isBoosterActive = state.isBoosterEnabled,
                boostPercentage = state.boostPercentage,
                isAudioPlaying = state.isTestAudioPlaying
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Test Toggle Bar
            AudioTestBar(
                isPlaying = state.isTestAudioPlaying,
                onToggleTest = { viewModel.toggleTestAudio() },
                onMuteToggle = { viewModel.toggleMute() },
                isMuted = state.isMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Navigation Section
            TabRowSection(
                selectedTab = state.selectedTab,
                onTabSelect = { viewModel.setSelectedTab(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (state.selectedTab) {
                0 -> EnhancersTab(
                    bassBoost = state.bassBoostStrength,
                    virtualizer = state.virtualizerStrength,
                    isEnabled = state.isBoosterEnabled,
                    onBassChange = { viewModel.setBassBoostStrength(it) },
                    onVirtChange = { viewModel.setVirtualizerStrength(it) }
                )
                1 -> EqualizerView(
                    selectedPreset = state.selectedPreset,
                    bandLevels = state.eqBandLevels,
                    isEnabled = state.isBoosterEnabled,
                    onPresetSelect = { viewModel.selectPreset(it) },
                    onBandChange = { band, level -> viewModel.setEqBandLevel(band, level) }
                )
                2 -> StreamVolumesView(
                    mediaVolume = state.mediaVolume,
                    maxMediaVolume = state.maxMediaVolume,
                    ringVolume = state.ringVolume,
                    maxRingVolume = state.maxRingVolume,
                    alarmVolume = state.alarmVolume,
                    maxAlarmVolume = state.maxAlarmVolume,
                    callVolume = state.callVolume,
                    maxCallVolume = state.maxCallVolume,
                    onVolumeChange = { stream, vol -> viewModel.setStreamVolume(stream, vol) }
                )
                3 -> SafetySettingsTab(
                    maxLimit = state.maxBoostLimit,
                    onLimitChange = { viewModel.setMaxBoostLimit(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // High Boost Safety Warning Dialog
    if (state.showWarningDialog) {
        SafetyWarningDialog(
            targetBoostLevel = state.pendingBoostLevel,
            onConfirm = { viewModel.confirmHighBoostWarning() },
            onDismiss = { viewModel.dismissWarningDialog() }
        )
    }
}

@Composable
private fun HeaderSection(
    isBoosterEnabled: Boolean,
    onToggleBooster: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isBoosterEnabled) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "App Icon",
                tint = if (isBoosterEnabled) NeonCyan else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "VOLUME BOOSTER",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isBoosterEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonMint.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonMint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Text(
                text = "Android 5+ Loudness Enhancer & EQ",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Switch(
            checked = isBoosterEnabled,
            onCheckedChange = onToggleBooster,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CyberSurfaceVariant
            ),
            modifier = Modifier.testTag("master_power_switch")
        )
    }
}

@Composable
private fun QuickBoostRow(
    currentBoost: Int,
    maxLimit: Int,
    isEnabled: Boolean,
    onBoostSelected: (Int) -> Unit
) {
    val quickLevels = listOf(0, 100, 125, 150, 175, 200).filter { it <= maxLimit }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickLevels.forEach { level ->
            val isSelected = currentBoost == level && isEnabled
            val buttonColor = when {
                !isEnabled -> CyberSurfaceVariant
                level == 0 -> CyberSurfaceVariant
                level > 150 -> NeonCoral
                level > 100 -> NeonAmber
                else -> NeonCyan
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) buttonColor else CyberSurface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) buttonColor else CyberBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("quick_boost_$level"),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { if (isEnabled) onBoostSelected(level) },
                    enabled = isEnabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSelected) Color.Black else TextPrimary
                    ),
                    border = null,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (level == 0) "OFF" else "$level%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioTestBar(
    isPlaying: Boolean,
    onToggleTest: () -> Unit,
    onMuteToggle: () -> Unit,
    isMuted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onToggleTest,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) NeonCoral else NeonCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("test_audio_button")
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Stop Test" else "Play Test",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPlaying) "Stop Test Synth" else "Test Audio Boost",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }

        IconButton(
            onClick = onMuteToggle,
            modifier = Modifier.testTag("quick_mute_button")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = "Mute Toggle",
                tint = if (isMuted) NeonCoral else NeonCyan
            )
        }
    }
}

@Composable
private fun TabRowSection(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val tabTitles = listOf("Enhancers", "Equalizer", "Streams", "Safety")
    val tabIcons = listOf(
        Icons.Default.SurroundSound,
        Icons.Default.Equalizer,
        Icons.Default.MusicNote,
        Icons.Default.Settings
    )

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = CyberSurface,
        contentColor = NeonCyan,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = NeonCyan,
                height = 3.dp
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag("audio_control_tabs")
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelect(index) },
                icon = {
                    Icon(
                        imageVector = tabIcons[index],
                        contentDescription = title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selectedContentColor = NeonCyan,
                unselectedContentColor = TextSecondary,
                modifier = Modifier.testTag("tab_$index")
            )
        }
    }
}

@Composable
private fun EnhancersTab(
    bassBoost: Int,
    virtualizer: Int,
    isEnabled: Boolean,
    onBassChange: (Int) -> Unit,
    onVirtChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp)
            .testTag("enhancers_tab_container")
    ) {
        Text(
            text = "AUDIO ENHANCERS",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bass Boost
        EnhancerSliderItem(
            title = "Bass Boost",
            subtitle = "Low-frequency punch & deep bass response",
            value = bassBoost,
            isEnabled = isEnabled,
            accentColor = NeonPurple,
            onValueChange = onBassChange,
            testTag = "bass_boost_slider"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3D Surround Virtualizer
        EnhancerSliderItem(
            title = "3D Surround Virtualizer",
            subtitle = "Spacial audio expansion for headphones & speakers",
            value = virtualizer,
            isEnabled = isEnabled,
            accentColor = NeonSky,
            onValueChange = onVirtChange,
            testTag = "virtualizer_slider"
        )
    }
}

@Composable
private fun EnhancerSliderItem(
    title: String,
    subtitle: String,
    value: Int,
    isEnabled: Boolean,
    accentColor: Color,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = if (isEnabled) "$value%" else "OFF",
                style = MaterialTheme.typography.titleMedium,
                color = if (isEnabled) accentColor else TextMuted,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value.toFloat(),
            onValueChange = { if (isEnabled) onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = if (isEnabled) accentColor else TextMuted,
                activeTrackColor = if (isEnabled) accentColor else TextMuted,
                inactiveTrackColor = CyberSurface
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SafetySettingsTab(
    maxLimit: Int,
    onLimitChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp)
            .testTag("safety_settings_container")
    ) {
        Text(
            text = "SAFETY & BOOST LIMITS",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Maximum Allowed Boost Level",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(100, 150, 200).forEach { limit ->
                val isSelected = maxLimit == limit
                Button(
                    onClick = { onLimitChange(limit) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NeonCyan else CyberSurfaceVariant,
                        contentColor = if (isSelected) Color.Black else TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("max_limit_button_$limit")
                ) {
                    Text(
                        text = "$limit%",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = NeonCyan,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "How Volume Boosting Works on Android 5+",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This app utilizes native Android LoudnessEnhancer and Equalizer audio effect APIs. It applies target gain directly to global audio streams without compromising sound clarity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
