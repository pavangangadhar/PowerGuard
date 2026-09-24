package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "powerguard_prefs")

class SecurityPreferences(private val context: Context) {

    companion object {
        val KEY_POWER_OFF_AUTH = booleanPreferencesKey("require_auth_power_off")
        val KEY_RESTART_AUTH = booleanPreferencesKey("require_auth_restart")
        val KEY_BOOT_AUTH = booleanPreferencesKey("require_auth_boot")
        val KEY_ONLY_WHEN_LOCKED = booleanPreferencesKey("protect_only_when_locked")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        val KEY_LAST_AUTH_SUCCESS = longPreferencesKey("last_auth_success_time")
    }

    val powerOffAuthEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_POWER_OFF_AUTH] ?: true
    }

    val restartAuthEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_RESTART_AUTH] ?: true
    }

    val bootAuthEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BOOT_AUTH] ?: false
    }

    val protectOnlyWhenLocked: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONLY_WHEN_LOCKED] ?: false
    }

    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_FEEDBACK] ?: true
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    val lastAuthSuccessTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_AUTH_SUCCESS] ?: 0L
    }

    suspend fun setPowerOffAuth(enabled: Boolean) {
        context.dataStore.edit { it[KEY_POWER_OFF_AUTH] = enabled }
    }

    suspend fun setRestartAuth(enabled: Boolean) {
        context.dataStore.edit { it[KEY_RESTART_AUTH] = enabled }
    }

    suspend fun setBootAuth(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BOOT_AUTH] = enabled }
    }

    suspend fun setProtectOnlyWhenLocked(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ONLY_WHEN_LOCKED] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = completed }
    }

    suspend fun updateLastAuthSuccess(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { it[KEY_LAST_AUTH_SUCCESS] = timestamp }
    }
}
