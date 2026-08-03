package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Watch
import androidx.compose.ui.graphics.vector.ImageVector

enum class DeviceType(val displayName: String) {
    HEADSET("Headphones / Earbuds"),
    GAMEPAD("Game Controller"),
    SMARTWATCH("Smartwatch"),
    KEYBOARD("Keyboard / Input"),
    MOUSE("Mouse / Pointer"),
    SPEAKER("Smart Speaker"),
    CAR("Car Audio System"),
    HEALTH("Fitness Tracker / Sensor"),
    SMARTPHONE("Smartphone / Tablet"),
    OTHER("Bluetooth Device");

    fun getIcon(): ImageVector {
        return when (this) {
            HEADSET -> Icons.Default.Headphones
            GAMEPAD -> Icons.Default.SportsEsports
            SMARTWATCH -> Icons.Default.Watch
            KEYBOARD -> Icons.Default.Keyboard
            MOUSE -> Icons.Default.Mouse
            SPEAKER -> Icons.Default.Speaker
            CAR -> Icons.Default.DirectionsCar
            HEALTH -> Icons.Default.FitnessCenter
            SMARTPHONE -> Icons.Default.Smartphone
            OTHER -> Icons.Default.Radio
        }
    }
}
