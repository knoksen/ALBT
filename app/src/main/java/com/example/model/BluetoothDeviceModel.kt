package com.example.model

data class BluetoothDeviceModel(
    val address: String,
    val name: String,
    val type: DeviceType,
    val rssiDbm: Int,
    val isConnected: Boolean = false,
    val isInstantPairEnabled: Boolean = true,
    val latencyMode: LatencyMode = LatencyMode.GAMING,
    val batteryLevel: Int = 85,
    val codec: CodecType = CodecType.LC3_LE,
    val distanceMeters: Float = 1.2f,
    val customAlias: String? = null,
    val priority: PriorityLevel = PriorityLevel.HIGH,
    val angleDegrees: Float = 45f // Position angle on radar visualization
)

enum class PriorityLevel(val label: String) {
    HIGH("High Priority (Low Latency)"),
    NORMAL("Normal Priority"),
    LOW("Background Sync")
}
