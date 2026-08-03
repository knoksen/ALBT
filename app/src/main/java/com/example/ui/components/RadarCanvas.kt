package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothDeviceModel
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarCanvas(
    devices: List<BluetoothDeviceModel>,
    isScanning: Boolean,
    proximityThresholdMeters: Float,
    onDeviceClick: (BluetoothDeviceModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val sweepAngle = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(isScanning) {
        if (isScanning) {
            sweepAngle.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            sweepAngle.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(CyberSurfaceVariant)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(devices) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val maxRadius = minOf(centerX, centerY) * 0.85f

                        // Find clicked device node
                        devices.forEach { device ->
                            val normDist = (device.distanceMeters / 3.0f).coerceIn(0.15f, 0.95f)
                            val radius = maxRadius * normDist
                            val angleRad = Math.toRadians(device.angleDegrees.toDouble())

                            val x = centerX + (radius * cos(angleRad)).toFloat()
                            val y = centerY + (radius * sin(angleRad)).toFloat()

                            val clickRadiusPx = 28.dp.toPx()
                            val distSq = (tapOffset.x - x) * (tapOffset.x - x) + (tapOffset.y - y) * (tapOffset.y - y)
                            if (distSq <= clickRadiusPx * clickRadiusPx) {
                                onDeviceClick(device)
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = minOf(centerX, centerY) * 0.85f

            // Concentric radar rings
            val ringRatios = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            val ringDistances = listOf("0.5m", "1.0m", "2.0m", "3.0m")

            ringRatios.forEachIndexed { index, ratio ->
                val r = maxRadius * ratio
                drawCircle(
                    color = Color(0x2200E5FF),
                    radius = r,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )

                // Distance labels
                drawText(
                    textMeasurer = textMeasurer,
                    text = ringDistances[index],
                    style = TextStyle(color = TextMuted, fontSize = 9.sp),
                    topLeft = Offset(centerX + 6f, centerY - r - 12f)
                )
            }

            // Proximity threshold zone
            val proximityNormDist = (proximityThresholdMeters / 3.0f).coerceIn(0.1f, 1.0f)
            val proxRadius = maxRadius * proximityNormDist
            drawCircle(
                color = Color(0x1A00E5FF),
                radius = proxRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0x6600E5FF),
                radius = proxRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            // Radar Crosshairs
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(centerX - maxRadius, centerY),
                end = Offset(centerX + maxRadius, centerY),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(centerX, centerY - maxRadius),
                end = Offset(centerX, centerY + maxRadius),
                strokeWidth = 1f
            )

            // Rotating Radar Beam Sweep
            if (isScanning) {
                val currentAngleRad = Math.toRadians(sweepAngle.value.toDouble())
                val beamEndX = centerX + (maxRadius * cos(currentAngleRad)).toFloat()
                val beamEndY = centerY + (maxRadius * sin(currentAngleRad)).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonCyan, Color.Transparent),
                        start = Offset(centerX, centerY),
                        end = Offset(beamEndX, beamEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(beamEndX, beamEndY),
                    strokeWidth = 3f
                )
            }

            // Central Phone Node
            drawCircle(
                color = NeonCyan,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(centerX, centerY)
            )

            // Device Nodes on Radar
            devices.forEach { device ->
                val normDist = (device.distanceMeters / 3.0f).coerceIn(0.15f, 0.95f)
                val radius = maxRadius * normDist
                val angleRad = Math.toRadians(device.angleDegrees.toDouble())

                val nodeX = centerX + (radius * cos(angleRad)).toFloat()
                val nodeY = centerY + (radius * sin(angleRad)).toFloat()

                val isConn = device.isConnected
                val nodeColor = when {
                    isConn -> MintEmerald
                    device.distanceMeters <= proximityThresholdMeters -> NeonCyan
                    else -> ElectricViolet
                }

                // Pulse ring for connected / in range
                if (isConn) {
                    drawCircle(
                        color = MintEmerald.copy(alpha = 0.3f),
                        radius = 16.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }

                // Core node circle
                drawCircle(
                    color = nodeColor,
                    radius = if (isConn) 9.dp.toPx() else 7.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )

                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )

                // Label
                val label = if (device.name.length > 12) device.name.take(10) + ".." else device.name
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = TextStyle(
                        color = if (isConn) MintEmerald else TextPrimary,
                        fontSize = 10.sp
                    ),
                    topLeft = Offset(nodeX - 20f, nodeY + 10f)
                )
            }
        }
    }
}
