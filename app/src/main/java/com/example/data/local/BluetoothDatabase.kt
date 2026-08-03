package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PairedDeviceEntity::class,
        ConnectionPresetEntity::class,
        SignalLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BluetoothDatabase : RoomDatabase() {
    abstract fun bluetoothDao(): BluetoothDao

    companion object {
        @Volatile
        private var INSTANCE: BluetoothDatabase? = null

        fun getDatabase(context: Context): BluetoothDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BluetoothDatabase::class.java,
                    "ultra_bluetooth_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
