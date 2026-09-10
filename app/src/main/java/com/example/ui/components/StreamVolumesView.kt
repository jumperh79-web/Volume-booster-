package com.example.ui.components

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StreamVolumesView(
    mediaVolume: Int,
    maxMediaVolume: Int,
    ringVolume: Int,
    maxRingVolume: Int,
    alarmVolume: Int,
    maxAlarmVolume: Int,
    callVolume: Int,
    maxCallVolume: Int,
    onVolumeChange: (streamType: Int, volume: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(16.dp)
            .testTag("stream_volumes_container")
    ) {
        Text(
            text = "SYSTEM STREAM VOLUMES",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        StreamVolumeItem(
            title = "Media",
            icon = Icons.Default.MusicNote,
            volume = mediaVolume,
            maxVolume = maxMediaVolume,
            onVolumeChange = { vol -> onVolumeChange(AudioManager.STREAM_MUSIC, vol) },
            testTag = "media_volume_slider"
        )

        Spacer(modifier = Modifier.height(12.dp))

        StreamVolumeItem(
            title = "Ringtone & Notifs",
            icon = Icons.Default.Notifications,
            volume = ringVolume,
            maxVolume = maxRingVolume,
            onVolumeChange = { vol -> onVolumeChange(AudioManager.STREAM_RING, vol) },
            testTag = "ring_volume_slider"
        )

        Spacer(modifier = Modifier.height(12.dp))

        StreamVolumeItem(
            title = "Alarm",
            icon = Icons.Default.Alarm,
            volume = alarmVolume,
            maxVolume = maxAlarmVolume,
            onVolumeChange = { vol -> onVolumeChange(AudioManager.STREAM_ALARM, vol) },
            testTag = "alarm_volume_slider"
        )

        Spacer(modifier = Modifier.height(12.dp))

        StreamVolumeItem(
            title = "Voice Call",
            icon = Icons.Default.Call,
            volume = callVolume,
            maxVolume = maxCallVolume,
            onVolumeChange = { vol -> onVolumeChange(AudioManager.STREAM_VOICE_CALL, vol) },
            testTag = "call_volume_slider"
        )
    }
}

@Composable
private fun StreamVolumeItem(
    title: String,
    icon: ImageVector,
    volume: Int,
    maxVolume: Int,
    onVolumeChange: (Int) -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$volume / $maxVolume",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onVolumeChange(0) }
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeMute,
                    contentDescription = "Mute Stream",
                    tint = TextMuted
                )
            }

            Slider(
                value = volume.toFloat(),
                onValueChange = { newValue -> onVolumeChange(newValue.toInt()) },
                valueRange = 0f..maxVolume.coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonSky,
                    inactiveTrackColor = CyberSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag)
            )

            IconButton(
                onClick = { onVolumeChange(maxVolume) }
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Max Stream Volume",
                    tint = NeonCyan
                )
            }
        }
    }
}
