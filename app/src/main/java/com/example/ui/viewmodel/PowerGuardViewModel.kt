package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SecurityEvent
import com.example.data.db.SecurityEventType
import com.example.data.repository.SecurityRepository
import com.example.security.DeviceSecurityDetector
import com.example.security.SecurityAuditReport
import com.example.ui.auth.PowerGuardAuthActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PowerGuardUiState(
    val powerOffProtectionEnabled: Boolean = true,
    val restartProtectionEnabled: Boolean = true,
    val bootProtectionEnabled: Boolean = false,
    val protectOnlyWhenLocked: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val auditReport: SecurityAuditReport? = null,
    val recentEvents: List<SecurityEvent> = emptyList(),
    val eventCount: Int = 0
)

class PowerGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SecurityRepository(application)

    private val _uiState = MutableStateFlow(PowerGuardUiState())
    val uiState: StateFlow<PowerGuardUiState> = _uiState.asStateFlow()

    init {
        refreshAudit()
        observePreferencesAndEvents()
    }

    fun refreshAudit() {
        val audit = DeviceSecurityDetector.performAudit(getApplication())
        _uiState.value = _uiState.value.copy(auditReport = audit)
    }

    private fun observePreferencesAndEvents() {
        viewModelScope.launch {
            repository.powerOffAuthEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(powerOffProtectionEnabled = enabled)
            }
        }
        viewModelScope.launch {
            repository.restartAuthEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(restartProtectionEnabled = enabled)
            }
        }
        viewModelScope.launch {
            repository.bootAuthEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(bootProtectionEnabled = enabled)
            }
        }
        viewModelScope.launch {
            repository.protectOnlyWhenLocked.collect { enabled ->
                _uiState.value = _uiState.value.copy(protectOnlyWhenLocked = enabled)
            }
        }
        viewModelScope.launch {
            repository.hapticFeedbackEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(hapticFeedbackEnabled = enabled)
            }
        }
        viewModelScope.launch {
            repository.onboardingCompleted.collect { completed ->
                _uiState.value = _uiState.value.copy(onboardingCompleted = completed)
            }
        }
        viewModelScope.launch {
            repository.allSecurityEvents.collect { events ->
                _uiState.value = _uiState.value.copy(recentEvents = events)
            }
        }
        viewModelScope.launch {
            repository.eventCount.collect { count ->
                _uiState.value = _uiState.value.copy(eventCount = count)
            }
        }
    }

    fun setPowerOffProtection(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPowerOffAuth(enabled)
        }
    }

    fun setRestartProtection(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRestartAuth(enabled)
        }
    }

    fun setBootProtection(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBootAuth(enabled)
        }
    }

    fun setProtectOnlyWhenLocked(enabled: Boolean) {
        viewModelScope.launch {
            repository.setProtectOnlyWhenLocked(enabled)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticFeedback(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearAllEvents()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun launchTestSimulation(actionType: SecurityEventType) {
        val context = getApplication<Application>()
        val intent = Intent(context, PowerGuardAuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PowerGuardAuthActivity.EXTRA_ACTION_TYPE, actionType.name)
            putExtra(PowerGuardAuthActivity.EXTRA_IS_SIMULATION, true)
        }
        context.startActivity(intent)
    }
}
