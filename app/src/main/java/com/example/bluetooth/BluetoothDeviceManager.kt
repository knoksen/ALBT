package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.model.BluetoothDeviceModel
import com.example.model.CodecType
import com.example.model.DeviceType
import com.example.model.LatencyMode
import com.example.model.PriorityLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BluetoothDeviceManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.TELECOM_SERVICE) as? BluetoothManager
            ?: context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceModel>> = _discoveredDevices.asStateFlow()

    private val _activeConnectedDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val activeConnectedDevices: StateFlow<List<BluetoothDeviceModel>> = _activeConnectedDevices.asStateFlow()

    private val _globalLatencyMode = MutableStateFlow(LatencyMode.GAMING)
    val globalLatencyMode: StateFlow<LatencyMode> = _globalLatencyMode.asStateFlow()

    private val _instantPairProximityThresholdMeters = MutableStateFlow(1.5f)
    val instantPairProximityThresholdMeters: StateFlow<Float> = _instantPairProximityThresholdMeters.asStateFlow()

    private val _isUltraPairEnabled = MutableStateFlow(true)
    val isUltraPairEnabled: StateFlow<Boolean> = _isUltraPairEnabled.asStateFlow()

    private val _simulatedLatencyMs = MutableStateFlow(32)
    val simulatedLatencyMs: StateFlow<Int> = _simulatedLatencyMs.asStateFlow()

    private val _isPingTesting = MutableStateFlow(false)
    val isPingTesting: StateFlow<Boolean> = _isPingTesting.asStateFlow()

    private val _lastPingResultMs = MutableStateFlow<Int?>(24)
    val lastPingResultMs: StateFlow<Int?> = _lastPingResultMs.asStateFlow()

    init {
        checkHardwareBluetoothState()
        initializeDefaultSimulatedDevices()
        startPeriodicSignalUpdateLoop()
    }

    fun checkHardwareBluetoothState() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled ?: true
    }

    fun hasRequiredPermissions(): Boolean {
        val connectPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        val scanPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        val locationPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return (connectPermission && scanPermission) || locationPermission
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true

        scope.launch {
            // Simulated scanning & real device discovery blend
            var scanCount = 0
            while (_isScanning.value && scanCount < 20) {
                delay(1200)
                scanCount++
                simulateDiscoveredDevice()
            }
            _isScanning.value = false
        }
    }

    fun stopScanning() {
        _isScanning.value = false
    }

    fun toggleScanning() {
        if (_isScanning.value) stopScanning() else startScanning()
    }

    fun toggleUltraPairing(enabled: Boolean) {
        _isUltraPairEnabled.value = enabled
    }

    fun setProximityThreshold(meters: Float) {
        _instantPairProximityThresholdMeters.value = meters
    }

    fun setGlobalLatencyMode(mode: LatencyMode) {
        _globalLatencyMode.value = mode
        _simulatedLatencyMs.value = mode.targetMs + Random.nextInt(-4, 5)

        // Update active devices to match latency mode
        _activeConnectedDevices.update { list ->
            list.map { it.copy(latencyMode = mode) }
        }
    }

    fun toggleDeviceConnection(device: BluetoothDeviceModel) {
        if (device.isConnected) {
            disconnectDevice(device.address)
        } else {
            connectDevice(device.address)
        }
    }

    fun connectDevice(address: String) {
        val target = _discoveredDevices.value.find { it.address == address }
            ?: _activeConnectedDevices.value.find { it.address == address } ?: return

        val updated = target.copy(
            isConnected = true,
            latencyMode = _globalLatencyMode.value
        )

        _activeConnectedDevices.update { list ->
            if (list.none { it.address == address }) list + updated else list.map { if (it.address == address) updated else it }
        }

        _discoveredDevices.update { list ->
            list.map { if (it.address == address) updated else it }
        }
    }

    fun disconnectDevice(address: String) {
        _activeConnectedDevices.update { list ->
            list.filterNot { it.address == address }
        }

        _discoveredDevices.update { list ->
            list.map { if (it.address == address) it.copy(isConnected = false) else it }
        }
    }

    fun instantPairAllInRange() {
        val threshold = _instantPairProximityThresholdMeters.value
        val readyToPair = _discoveredDevices.value.filter {
            !it.isConnected && it.distanceMeters <= threshold
        }

        readyToPair.forEach { device ->
            connectDevice(device.address)
        }
    }

    fun setDevicePriority(address: String, priority: PriorityLevel) {
        _activeConnectedDevices.update { list ->
            list.map { if (it.address == address) it.copy(priority = priority) else it }
        }
        _discoveredDevices.update { list ->
            list.map { if (it.address == address) it.copy(priority = priority) else it }
        }
    }

    fun setDeviceCodec(address: String, codec: CodecType) {
        _activeConnectedDevices.update { list ->
            list.map { if (it.address == address) it.copy(codec = codec) else it }
        }
    }

    fun runLatencyPingTest() {
        if (_isPingTesting.value) return
        _isPingTesting.value = true

        scope.launch {
            delay(300)
            val baseMs = _globalLatencyMode.value.targetMs
            val measuredMs = max(12, baseMs + Random.nextInt(-6, 7))
            _lastPingResultMs.value = measuredMs
            _simulatedLatencyMs.value = measuredMs
            delay(400)
            _isPingTesting.value = false
        }
    }

    private fun initializeDefaultSimulatedDevices() {
        val initialList = listOf(
            BluetoothDeviceModel(
                address = "70:88:6B:1A:00:E1",
                name = "AeroPro Wireless ANC",
                type = DeviceType.HEADSET,
                rssiDbm = -48,
                isConnected = true,
                batteryLevel = 92,
                codec = CodecType.LC3_LE,
                distanceMeters = 0.6f,
                angleDegrees = 30f,
                priority = PriorityLevel.HIGH
            ),
            BluetoothDeviceModel(
                address = "50:C1:3D:88:FF:9C",
                name = "Apex Gamepad Pro",
                type = DeviceType.GAMEPAD,
                rssiDbm = -54,
                isConnected = true,
                batteryLevel = 78,
                codec = CodecType.LC3_LE,
                distanceMeters = 0.8f,
                angleDegrees = 140f,
                priority = PriorityLevel.HIGH
            ),
            BluetoothDeviceModel(
                address = "A4:20:C2:E4:31:07",
                name = "Horizon SmartWatch Ultra",
                type = DeviceType.SMARTWATCH,
                rssiDbm = -62,
                isConnected = false,
                batteryLevel = 65,
                codec = CodecType.AAC,
                distanceMeters = 1.4f,
                angleDegrees = 220f,
                priority = PriorityLevel.NORMAL
            ),
            BluetoothDeviceModel(
                address = "88:DF:99:A1:2B:11",
                name = "MechKeys Pro Wireless",
                type = DeviceType.KEYBOARD,
                rssiDbm = -68,
                isConnected = false,
                batteryLevel = 88,
                codec = CodecType.SBC,
                distanceMeters = 1.8f,
                angleDegrees = 300f,
                priority = PriorityLevel.NORMAL
            ),
            BluetoothDeviceModel(
                address = "34:E6:D1:20:FA:82",
                name = "Studio SoundFlex Speaker",
                type = DeviceType.SPEAKER,
                rssiDbm = -75,
                isConnected = false,
                batteryLevel = 45,
                codec = CodecType.LDAC,
                distanceMeters = 2.5f,
                angleDegrees = 85f,
                priority = PriorityLevel.LOW
            )
        )

        _discoveredDevices.value = initialList
        _activeConnectedDevices.value = initialList.filter { it.isConnected }
    }

    private fun simulateDiscoveredDevice() {
        val pool = listOf(
            Triple("Pulse HR Fitness Ring", DeviceType.HEALTH, CodecType.LC3_LE),
            Triple("CyberMouse Precision", DeviceType.MOUSE, CodecType.SBC),
            Triple("Tesla Car Audio Stream", DeviceType.CAR, CodecType.LDAC),
            Triple("UltraBuds Pro 2", DeviceType.HEADSET, CodecType.APTX_ADAPTIVE),
            Triple("Retro Arcade Pad", DeviceType.GAMEPAD, CodecType.LC3_LE)
        )

        val currentAddresses = _discoveredDevices.value.map { it.address }
        val randomCandidate = pool.random()
        val randomMac = generateRandomMacAddress()

        if (currentAddresses.size < 10) {
            val newDevice = BluetoothDeviceModel(
                address = randomMac,
                name = randomCandidate.first,
                type = randomCandidate.second,
                rssiDbm = Random.nextInt(-85, -50),
                isConnected = false,
                batteryLevel = Random.nextInt(20, 100),
                codec = randomCandidate.third,
                distanceMeters = Random.nextFloat() * 2.5f + 0.3f,
                angleDegrees = Random.nextFloat() * 360f
            )

            _discoveredDevices.update { it + newDevice }

            // Auto instant pair if enabled and in range
            if (_isUltraPairEnabled.value && newDevice.distanceMeters <= _instantPairProximityThresholdMeters.value) {
                connectDevice(newDevice.address)
            }
        }
    }

    private fun startPeriodicSignalUpdateLoop() {
        scope.launch {
            while (true) {
                delay(2000)
                _discoveredDevices.update { list ->
                    list.map { dev ->
                        val rssiDelta = Random.nextInt(-3, 4)
                        val newRssi = (dev.rssiDbm + rssiDelta).coerceIn(-95, -35)
                        // Distance estimation formula from dBm: dist ~ 10^((TxPower - RSSI) / 20)
                        val estDist = min(4.0f, max(0.2f, Math.pow(10.0, (-45.0 - newRssi) / 20.0).toFloat()))
                        dev.copy(
                            rssiDbm = newRssi,
                            distanceMeters = estDist
                        )
                    }
                }
            }
        }
    }

    private fun generateRandomMacAddress(): String {
        return (1..6).joinToString(":") { "%02X".format(Random.nextInt(0, 256)) }
    }
}
