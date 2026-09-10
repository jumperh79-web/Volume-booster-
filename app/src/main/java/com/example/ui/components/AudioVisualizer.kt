package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextMuted
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    isBoosterActive: Boolean,
    boostPercentage: Int,
    isAudioPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "visualizer_anim")
    val animPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CyberSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("audio_visualizer_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barCount = 24
            val spacing = 4.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = (size.width - totalSpacing) / barCount

            val boostRatio = (boostPercentage / 200f).coerceIn(0.1f, 1f)
            val isLive = isBoosterActive && (isAudioPlaying || boostPercentage > 0)

            for (i in 0 until barCount) {
                // Calculate dynamic animated height for bar
                val frequencyFactor = (i.toFloat() / barCount.toFloat())
                val sineWave1 = sin(animPhase + i * 0.4f)
                val sineWave2 = sin(animPhase * 1.5f + i * 0.8f)

                val rawHeightFactor = if (isLive) {
                    ((sineWave1 * 0.4f + sineWave2 * 0.3f + 0.5f) * boostRatio).coerceIn(0.12f, 0.95f)
                } else {
                    0.06f // Static resting state bar height
                }

                val barHeight = size.height * rawHeightFactor
                val x = i * (barWidth + spacing)
                val y = size.height - barHeight

                val barColor = when {
                    !isLive -> TextMuted.copy(alpha = 0.3f)
                    frequencyFactor > 0.8f -> NeonCoral
                    frequencyFactor > 0.5f -> NeonAmber
                    else -> NeonCyan
                }

                val gradient = Brush.verticalGradient(
                    colors = listOf(barColor, NeonSky, CyberBorder),
                    startY = y,
                    endY = size.height
                )

                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Peak dot above bar
                if (isLive) {
                    val dotY = (y - 6.dp.toPx()).coerceAtLeast(0f)
                    drawCircle(
                        color = barColor,
                        radius = (barWidth / 3f).coerceAtMost(3.dp.toPx()),
                        center = Offset(x + barWidth / 2f, dotY)
                    )
                }
            }
        }
    }
}
