package com.example.data.repository

import android.content.Context
import com.example.data.db.AuthResultStatus
import com.example.data.db.PowerGuardDatabase
import com.example.data.db.SecurityEvent
import com.example.data.db.SecurityEventType
import com.example.data.preferences.SecurityPreferences
import kotlinx.coroutines.flow.Flow

class SecurityRepository(context: Context) {
    private val database = PowerGuardDatabase.getInstance(context)
    private val dao = database.securityEventDao()
    private val preferences = SecurityPreferences(context)

    val powerOffAuthEnabled: Flow<Boolean> = preferences.powerOffAuthEnabled
    val restartAuthEnabled: Flow<Boolean> = preferences.restartAuthEnabled
    val bootAuthEnabled: Flow<Boolean> = preferences.bootAuthEnabled
    val protectOnlyWhenLocked: Flow<Boolean> = preferences.protectOnlyWhenLocked
    val hapticFeedbackEnabled: Flow<Boolean> = preferences.hapticFeedbackEnabled
    val onboardingCompleted: Flow<Boolean> = preferences.onboardingCompleted
    val lastAuthSuccessTime: Flow<Long> = preferences.lastAuthSuccessTime

    val allSecurityEvents: Flow<List<SecurityEvent>> = dao.getAllEvents()
    val eventCount: Flow<Int> = dao.getEventCount()

    suspend fun setPowerOffAuth(enabled: Boolean) {
        preferences.setPowerOffAuth(enabled)
        logSecurityEvent(
            eventType = SecurityEventType.PROTECTION_TOGGLED,
            authStatus = AuthResultStatus.SUCCESS,
            authMethod = "System",
            details = "Power-off protection toggled to ${if (enabled) "ON" else "OFF"}"
        )
    }

    suspend fun setRestartAuth(enabled: Boolean) {
        preferences.setRestartAuth(enabled)
        logSecurityEvent(
            eventType = SecurityEventType.PROTECTION_TOGGLED,
            authStatus = AuthResultStatus.SUCCESS,
            authMethod = "System",
            details = "Restart protection toggled to ${if (enabled) "ON" else "OFF"}"
        )
    }

    suspend fun setBootAuth(enabled: Boolean) {
        preferences.setBootAuth(enabled)
        logSecurityEvent(
            eventType = SecurityEventType.SETTINGS_CHANGED,
            authStatus = AuthResultStatus.SUCCESS,
            authMethod = "System",
            details = "Require authentication after boot set to ${if (enabled) "ON" else "OFF"}"
        )
    }

    suspend fun setProtectOnlyWhenLocked(enabled: Boolean) {
        preferences.setProtectOnlyWhenLocked(enabled)
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        preferences.setHapticFeedback(enabled)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboardingCompleted(completed)
    }

    suspend fun recordSuccessfulAuth() {
        preferences.updateLastAuthSuccess()
    }

    suspend fun logSecurityEvent(
        eventType: SecurityEventType,
        authStatus: AuthResultStatus,
        authMethod: String,
        details: String,
        isSimulated: Boolean = false
    ) {
        val event = SecurityEvent(
            eventType = eventType,
            authStatus = authStatus,
            authMethod = authMethod,
            details = details,
            isSimulated = isSimulated
        )
        dao.insertEvent(event)
    }

    suspend fun clearAllEvents() {
        dao.clearAllEvents()
    }

    suspend fun deleteEventById(id: Long) {
        dao.deleteEventById(id)
    }
}
