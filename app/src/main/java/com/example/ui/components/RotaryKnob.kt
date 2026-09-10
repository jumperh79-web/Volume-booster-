package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonSky
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RotaryKnob(
    value: Int, // 0 to maxLimit
    maxLimit: Int, // 100, 150, 200
    isEnabled: Boolean,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val startAngle = 135f
    val sweepAngle = 270f

    val currentFraction = (value.toFloat() / maxLimit.toFloat()).coerceIn(0f, 1f)
    val activeColor = when {
        !isEnabled -> TextMuted
        value > 150 -> NeonCoral
        value > 100 -> NeonAmber
        else -> NeonCyan
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Decrease button
            IconButton(
                onClick = {
                    if (isEnabled) onValueChange((value - 5).coerceAtLeast(0))
                },
                enabled = isEnabled && value > 0,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CyberSurfaceVariant,
                    contentColor = TextPrimary,
                    disabledContainerColor = CyberSurface
                ),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("knob_decrease_button")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease Boost")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center Circular Knob Canvas
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .shadow(12.dp, CircleShape, ambientColor = activeColor, spotColor = activeColor)
                    .background(CyberSurface)
                    .pointerInput(isEnabled, maxLimit) {
                        if (!isEnabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = change.position
                            val dx = touch.x - center.x
                            val dy = touch.y - center.y

                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            // Convert angle relative to startAngle (135 deg)
                            var relativeAngle = angle - startAngle
                            if (relativeAngle < 0) relativeAngle += 360f

                            if (relativeAngle <= sweepAngle) {
                                val frac = (relativeAngle / sweepAngle).coerceIn(0f, 1f)
                                val newValue = (frac * maxLimit).roundToInt()
                                onValueChange(newValue)
                            }
                        }
                    }
                    .testTag("rotary_volume_knob"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    val strokeWidth = 18.dp.toPx()
                    val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Track background arc
                    drawArc(
                        color = CyberBorder,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc with glowing gradient
                    if (isEnabled && currentFraction > 0f) {
                        val activeSweep = sweepAngle * currentFraction
                        val gradientBrush = Brush.sweepGradient(
                            listOf(NeonSky, NeonCyan, activeColor)
                        )
                        drawArc(
                            brush = gradientBrush,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Pointer indicator knob dot
                        val endAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
                        val indicatorRadius = radius - strokeWidth / 2f
                        val dotX = (center.x + indicatorRadius * cos(endAngleRad)).toFloat()
                        val dotY = (center.y + indicatorRadius * sin(endAngleRad)).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = activeColor,
                            radius = 8.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // Center Text Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BOOST",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isEnabled) "$value%" else "OFF",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isEnabled) activeColor else TextMuted
                    )
                    Text(
                        text = when {
                            !isEnabled -> "Booster Disabled"
                            value == 0 -> "Muted"
                            value <= 100 -> "Safe Output"
                            value <= 150 -> "High Gain"
                            else -> "SUPER BOOST"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) activeColor else TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Increase button
            IconButton(
                onClick = {
                    if (isEnabled) onValueChange((value + 5).coerceAtMost(maxLimit))
                },
                enabled = isEnabled && value < maxLimit,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CyberSurfaceVariant,
                    contentColor = TextPrimary,
                    disabledContainerColor = CyberSurface
                ),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("knob_increase_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase Boost")
            }
        }
    }
}
