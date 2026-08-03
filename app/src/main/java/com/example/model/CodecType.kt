package com.example.model

enum class CodecType(
    val fullName: String,
    val maxBitrateKbps: Int,
    val isLeAudio: Boolean
) {
    LC3_LE("LC3 (Bluetooth LE Audio)", 384, true),
    APTX_ADAPTIVE("aptX Adaptive / LL", 420, false),
    LDAC("LDAC Lossless", 990, false),
    AAC("AAC Stereo", 320, false),
    SBC("SBC Standard", 328, false)
}
