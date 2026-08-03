package com.example.data.repository

import com.example.data.local.BluetoothDao
import com.example.data.local.ConnectionPresetEntity
import com.example.data.local.PairedDeviceEntity
import com.example.data.local.SignalLogEntity
import kotlinx.coroutines.flow.Flow

class BluetoothRepository(private val bluetoothDao: BluetoothDao) {
    val pairedDevices: Flow<List<PairedDeviceEntity>> = bluetoothDao.getAllPairedDevices()
    val presets: Flow<List<ConnectionPresetEntity>> = bluetoothDao.getAllPresets()
    val recentSignalLogs: Flow<List<SignalLogEntity>> = bluetoothDao.getAllRecentSignalLogs()

    suspend fun saveDevice(device: PairedDeviceEntity) {
        bluetoothDao.insertOrUpdateDevice(device)
    }

    suspend fun removeDevice(device: PairedDeviceEntity) {
        bluetoothDao.deleteDevice(device)
    }

    suspend fun savePreset(preset: ConnectionPresetEntity) {
        bluetoothDao.insertPreset(preset)
    }

    suspend fun removePreset(preset: ConnectionPresetEntity) {
        bluetoothDao.deletePreset(preset)
    }

    suspend fun logSignal(log: SignalLogEntity) {
        bluetoothDao.insertSignalLog(log)
    }

    fun getLogsForDevice(address: String): Flow<List<SignalLogEntity>> {
        return bluetoothDao.getSignalLogsForDevice(address)
    }
}
