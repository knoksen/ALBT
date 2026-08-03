package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodecType
import com.example.model.LatencyMode
import com.example.ui.components.LatencyPingMeter
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BluetoothViewModel

@Composable
fun LatencyLabScreen(
    viewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.globalLatencyMode.collectAsState()
    val isTesting by viewModel.isPingTesting.collectAsState()
    val lastPingResultMs by viewModel.lastPingResultMs.collectAsState()
    val activeDevices by viewModel.activeDevices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Realtime Latency Ping Meter
        item {
            LatencyPingMeter(
                currentMode = currentMode,
                measuredMs = lastPingResultMs,
                isTesting = isTesting,
                onRunTest = { viewModel.runLatencyPingTest() }
            )
        }

        // Mode Selector Header
        item {
            Text(
                text = "GLOBAL LATENCY MODES",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextMuted
            )
        }

        // Latency Mode Cards
        items(LatencyMode.entries.toTypedArray()) { mode ->
            val isSelected = currentMode == mode
            val borderColor = if (isSelected) NeonCyan else CyberCardBorder

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { viewModel.setGlobalLatencyMode(mode) }
                    .testTag("latency_mode_card_${mode.name}"),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setGlobalLatencyMode(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NeonCyan,
                                unselectedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${mode.targetMs}ms Target",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (mode.targetMs <= 40) MintEmerald else ElectricViolet
                                )
                            }
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Codec & Audio Buffer Tuning Section Header
        item {
            Text(
                text = "AUDIO CODEC & BUFFER PARAMETERS",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextMuted
            )
        }

        // Codec Manager for Active Devices
        if (activeDevices.isEmpty()) {
            item {
                Text(
                    text = "Connect a device on the Radar tab to configure codec & buffer parameters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            items(activeDevices, key = { it.address }) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Codec: ${device.codec.fullName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Audio Codec:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CodecType.entries.forEach { codec ->
                                val isSelected = device.codec == codec
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) NeonCyan else CyberSurfaceVariant)
                                        .clickable { viewModel.setDeviceCodec(device.address, codec) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("codec_option_${codec.name}")
                                ) {
                                    Text(
                                        text = codec.name.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) CyberBackground else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
