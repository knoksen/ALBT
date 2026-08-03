package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDao {
    @Query("SELECT * FROM paired_devices ORDER BY lastConnectedTimestamp DESC")
    fun getAllPairedDevices(): Flow<List<PairedDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: PairedDeviceEntity)

    @Delete
    suspend fun deleteDevice(device: PairedDeviceEntity)

    @Query("SELECT * FROM connection_presets")
    fun getAllPresets(): Flow<List<ConnectionPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ConnectionPresetEntity)

    @Delete
    suspend fun deletePreset(preset: ConnectionPresetEntity)

    @Query("SELECT * FROM signal_logs WHERE deviceAddress = :address ORDER BY timestamp DESC LIMIT 50")
    fun getSignalLogsForDevice(address: String): Flow<List<SignalLogEntity>>

    @Query("SELECT * FROM signal_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllRecentSignalLogs(): Flow<List<SignalLogEntity>>

    @Insert
    suspend fun insertSignalLog(log: SignalLogEntity)
}
