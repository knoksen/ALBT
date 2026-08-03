package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_presets")
data class ConnectionPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetName: String,
    val iconName: String,
    val targetDeviceAddresses: String, // Comma separated list of addresses
    val latencyMode: String,
    val isActive: Boolean = false
)
