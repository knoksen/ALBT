package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothDeviceManager
import com.example.data.local.BluetoothDatabase
import com.example.data.local.ConnectionPresetEntity
import com.example.data.local.PairedDeviceEntity
import com.example.data.local.SignalLogEntity
import com.example.data.repository.BluetoothRepository
import com.example.model.BluetoothDeviceModel
import com.example.model.CodecType
import com.example.model.DeviceType
import com.example.model.LatencyMode
import com.example.model.PriorityLevel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    val deviceManager = BluetoothDeviceManager(application.applicationContext)
    private val repository: BluetoothRepository

    val discoveredDevices: StateFlow<List<BluetoothDeviceModel>> = deviceManager.discoveredDevices
    val activeDevices: StateFlow<List<BluetoothDeviceModel>> = deviceManager.activeConnectedDevices
    val isScanning: StateFlow<Boolean> = deviceManager.isScanning
    val isBluetoothEnabled: StateFlow<Boolean> = deviceManager.isBluetoothEnabled
    val globalLatencyMode: StateFlow<LatencyMode> = deviceManager.globalLatencyMode
    val isUltraPairEnabled: StateFlow<Boolean> = deviceManager.isUltraPairEnabled
    val proximityThresholdMeters: StateFlow<Float> = deviceManager.instantPairProximityThresholdMeters
    val simulatedLatencyMs: StateFlow<Int> = deviceManager.simulatedLatencyMs
    val isPingTesting: StateFlow<Boolean> = deviceManager.isPingTesting
    val lastPingResultMs: StateFlow<Int?> = deviceManager.lastPingResultMs

    val savedPresets: StateFlow<List<ConnectionPresetEntity>>
    val recentSignalLogs: StateFlow<List<SignalLogEntity>>

    init {
        val dao = BluetoothDatabase.getDatabase(application).bluetoothDao()
        repository = BluetoothRepository(dao)

        savedPresets = repository.presets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recentSignalLogs = repository.recentSignalLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Pre-populate sample presets if database is empty
        viewModelScope.launch {
            repository.presets.collect { list ->
                if (list.isEmpty()) {
                    populateDefaultPresets()
                }
            }
        }
    }

    fun startScanning() = deviceManager.startScanning()
    fun stopScanning() = deviceManager.stopScanning()
    fun toggleScanning() = deviceManager.toggleScanning()

    fun toggleDeviceConnection(device: BluetoothDeviceModel) {
        deviceManager.toggleDeviceConnection(device)
        viewModelScope.launch {
            if (!device.isConnected) {
                // Save to Room on connect
                val entity = PairedDeviceEntity(
                    address = device.address,
                    name = device.name,
                    type = device.type.name,
                    isInstantPairEnabled = device.isInstantPairEnabled,
                    preferredLatencyMode = device.latencyMode.name,
                    priorityLevel = device.priority.name,
                    customAlias = device.customAlias,
                    lastConnectedTimestamp = System.currentTimeMillis(),
                    batteryLevel = device.batteryLevel,
                    codecType = device.codec.name
                )
                repository.saveDevice(entity)

                // Log signal entry
                repository.logSignal(
                    SignalLogEntity(
                        deviceAddress = device.address,
                        rssiDbm = device.rssiDbm,
                        latencyMs = device.latencyMode.targetMs
                    )
                )
            }
        }
    }

    fun instantPairAllInRange() = deviceManager.instantPairAllInRange()

    fun setGlobalLatencyMode(mode: LatencyMode) = deviceManager.setGlobalLatencyMode(mode)

    fun toggleUltraPairing(enabled: Boolean) = deviceManager.toggleUltraPairing(enabled)

    fun setProximityThreshold(meters: Float) = deviceManager.setProximityThreshold(meters)

    fun setDevicePriority(address: String, priority: PriorityLevel) = deviceManager.setDevicePriority(address, priority)

    fun setDeviceCodec(address: String, codec: CodecType) = deviceManager.setDeviceCodec(address, codec)

    fun runLatencyPingTest() = deviceManager.runLatencyPingTest()

    fun applyPreset(preset: ConnectionPresetEntity) {
        val targetAddresses = preset.targetDeviceAddresses.split(",").map { it.trim() }
        val targetLatencyMode = try {
            LatencyMode.valueOf(preset.latencyMode)
        } catch (e: Exception) {
            LatencyMode.GAMING
        }

        setGlobalLatencyMode(targetLatencyMode)

        targetAddresses.forEach { addr ->
            if (addr.isNotEmpty()) {
                deviceManager.connectDevice(addr)
            }
        }
    }

    fun saveNewPreset(name: String, icon: String, deviceAddresses: List<String>, latencyMode: LatencyMode) {
        viewModelScope.launch {
            repository.savePreset(
                ConnectionPresetEntity(
                    presetName = name,
                    iconName = icon,
                    targetDeviceAddresses = deviceAddresses.joinToString(","),
                    latencyMode = latencyMode.name,
                    isActive = false
                )
            )
        }
    }

    private suspend fun populateDefaultPresets() {
        val presets = listOf(
            ConnectionPresetEntity(
                presetName = "Pro Gaming Rig",
                iconName = "gamepad",
                targetDeviceAddresses = "70:88:6B:1A:00:E1,50:C1:3D:88:FF:9C",
                latencyMode = "ULTRA_LOW"
            ),
            ConnectionPresetEntity(
                presetName = "Workstation Focus",
                iconName = "keyboard",
                targetDeviceAddresses = "88:DF:99:A1:2B:11,70:88:6B:1A:00:E1",
                latencyMode = "BALANCED"
            ),
            ConnectionPresetEntity(
                presetName = "Gym & Workout",
                iconName = "watch",
                targetDeviceAddresses = "70:88:6B:1A:00:E1,A4:20:C2:E4:31:07",
                latencyMode = "GAMING"
            ),
            ConnectionPresetEntity(
                presetName = "Cinema Dual Stream",
                iconName = "speaker",
                targetDeviceAddresses = "70:88:6B:1A:00:E1,34:E6:D1:20:FA:82",
                latencyMode = "HIGH_FIDELITY"
            )
        )
        presets.forEach { repository.savePreset(it) }
    }
}
