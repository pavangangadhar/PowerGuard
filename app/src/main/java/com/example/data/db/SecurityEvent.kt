package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SecurityEventType {
    POWER_OFF_ATTEMPT,
    RESTART_ATTEMPT,
    PROTECTION_TOGGLED,
    SETTINGS_CHANGED,
    DEVICE_BOOT,
    TEST_SIMULATION
}

enum class AuthResultStatus {
    SUCCESS,
    FAILED,
    CANCELLED,
    BLOCKED
}

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: SecurityEventType,
    val authStatus: AuthResultStatus,
    val authMethod: String,
    val details: String,
    val isSimulated: Boolean = false
)
