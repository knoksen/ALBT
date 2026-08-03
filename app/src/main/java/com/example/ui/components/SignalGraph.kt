package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SignalLogEntity
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UltraAmber

@Composable
fun SignalGraph(
    signalLogs: List<SignalLogEntity>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Signal Graph",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2.4GHz Signal & Spectrum Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Live dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintEmerald
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(CyberSurfaceVariant)
            ) {
                val w = size.width
                val h = size.height

                // Draw Grid lines
                val gridLevels = listOf(-30, -50, -70, -90)
                gridLevels.forEachIndexed { idx, dbm ->
                    val y = h * (idx / 3.0f)
                    drawLine(
                        color = Color(0x1F00E5FF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "$dbm dBm",
                        style = TextStyle(color = TextMuted, fontSize = 9.sp),
                        topLeft = Offset(8f, y - 10f)
                    )
                }

                // Plot Signal Wave
                val points = if (signalLogs.isEmpty()) {
                    // Fallback synthetic telemetry wave
                    listOf(-42, -45, -48, -40, -52, -44, -46, -38, -45, -49, -43)
                } else {
                    signalLogs.map { it.rssiDbm }
                }

                if (points.isNotEmpty()) {
                    val path = Path()
                    val stepX = w / (points.size - 1).coerceAtLeast(1)

                    points.forEachIndexed { i, dbm ->
                        val normY = ((dbm - (-90f)) / ((-30f) - (-90f))).coerceIn(0f, 1f)
                        val x = i * stepX
                        val y = h - (normY * h)

                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        // Draw point dot
                        drawCircle(
                            color = NeonCyan,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = Stroke(width = 2.5f.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Interference: Low (Channel 6)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Quality Score: 98%", style = MaterialTheme.typography.bodySmall, color = MintEmerald)
            }
        }
    }
}
