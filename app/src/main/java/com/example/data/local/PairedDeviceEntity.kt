package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey val address: String,
    val name: String,
    val type: String,
    val isInstantPairEnabled: Boolean,
    val preferredLatencyMode: String,
    val priorityLevel: String,
    val customAlias: String?,
    val lastConnectedTimestamp: Long,
    val batteryLevel: Int,
    val codecType: String
)
