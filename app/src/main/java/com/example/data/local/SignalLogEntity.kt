package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signal_logs")
data class SignalLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceAddress: String,
    val rssiDbm: Int,
    val latencyMs: Int,
    val timestamp: Long = System.currentTimeMillis()
)
