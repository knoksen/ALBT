package com.example.model

enum class LatencyMode(
    val title: String,
    val targetMs: Int,
    val description: String,
    val recommendedFor: String
) {
    ULTRA_LOW("Ultra Low Latency", 20, "20ms Audio/Input direct sync buffer", "Competitive Gaming & VR"),
    GAMING("Gaming Mode", 40, "40ms Low latency audio stream", "Action Games & Video Sync"),
    BALANCED("Balanced Mode", 80, "80ms Optimized connection stability", "Streaming & Daily Use"),
    HIGH_FIDELITY("High Fidelity", 150, "150ms High bitrate lossless sync", "Lossless Music & Hi-Fi Audio")
}
