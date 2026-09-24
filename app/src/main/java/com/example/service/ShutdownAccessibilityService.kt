package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEventType
import com.example.data.preferences.SecurityPreferences
import com.example.data.repository.SecurityRepository
import com.example.ui.auth.PowerGuardAuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShutdownAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: SecurityRepository
    private lateinit var preferences: SecurityPreferences

    // Prevents duplicate triggers within 2 seconds
    private var lastTriggerTime = 0L

    override fun onCreate() {
        super.onCreate()
        repository = SecurityRepository(applicationContext)
        preferences = SecurityPreferences(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTriggerTime < 2000) {
            return
        }

        val eventText = event.text.joinToString(" ").lowercase()
        val contentDesc = (event.contentDescription ?: "").toString().lowercase()
        val className = (event.className ?: "").toString().lowercase()

        // Check if event relates to power menu or shutdown/restart button
        val isPowerMenu = className.contains("globalactions") ||
                className.contains("powerdialog") ||
                eventText.contains("power off") ||
                eventText.contains("restart") ||
                eventText.contains("reboot") ||
                contentDesc.contains("power off") ||
                contentDesc.contains("restart")

        if (!isPowerMenu) {
            // Also inspect node tree if available
            rootInActiveWindow?.let { rootNode ->
                val detectedAction = inspectNodesForPowerAction(rootNode)
                if (detectedAction != null) {
                    handlePowerIntercept(detectedAction)
                }
            }
            return
        }

        val action = when {
            eventText.contains("power off") || contentDesc.contains("power off") -> SecurityEventType.POWER_OFF_ATTEMPT
            eventText.contains("restart") || eventText.contains("reboot") || contentDesc.contains("restart") -> SecurityEventType.RESTART_ATTEMPT
            else -> SecurityEventType.POWER_OFF_ATTEMPT
        }

        handlePowerIntercept(action)
    }

    private fun inspectNodesForPowerAction(node: AccessibilityNodeInfo): SecurityEventType? {
        val text = (node.text ?: "").toString().lowercase()
        val desc = (node.contentDescription ?: "").toString().lowercase()
        val viewId = (node.viewIdResourceName ?: "").lowercase()

        if (text.contains("power off") || desc.contains("power off") || viewId.contains("poweroff") || viewId.contains("shutdown")) {
            return SecurityEventType.POWER_OFF_ATTEMPT
        }
        if (text.contains("restart") || desc.contains("restart") || text.contains("reboot") || viewId.contains("restart") || viewId.contains("reboot")) {
            return SecurityEventType.RESTART_ATTEMPT
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = inspectNodesForPowerAction(child)
            if (found != null) return found
        }
        return null
    }

    private fun handlePowerIntercept(actionType: SecurityEventType) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 2500) return
        lastTriggerTime = now

        serviceScope.launch {
            val powerOffProtected = repository.powerOffAuthEnabled.first()
            val restartProtected = repository.restartAuthEnabled.first()
            val lastAuthSuccess = repository.lastAuthSuccessTime.first()

            // If user authenticated within last 15 seconds, permit action
            if (now - lastAuthSuccess < 15_000) {
                return@launch
            }

            val shouldIntercept = when (actionType) {
                SecurityEventType.POWER_OFF_ATTEMPT -> powerOffProtected
                SecurityEventType.RESTART_ATTEMPT -> restartProtected
                else -> false
            }

            if (shouldIntercept) {
                // Dismiss the power menu immediately
                performGlobalAction(GLOBAL_ACTION_BACK)

                // Vibrate if enabled
                val haptic = repository.hapticFeedbackEnabled.first()
                if (haptic) {
                    triggerHapticFeedback()
                }

                // Log intercept attempt
                repository.logSecurityEvent(
                    eventType = actionType,
                    authStatus = AuthResultStatus.BLOCKED,
                    authMethod = "Pending Device Lock",
                    details = "Unauthorized ${if (actionType == SecurityEventType.POWER_OFF_ATTEMPT) "power-off" else "restart"} detected; intercepted by PowerGuard"
                )

                // Launch full screen secure authentication activity
                val intent = Intent(this@ShutdownAccessibilityService, PowerGuardAuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(PowerGuardAuthActivity.EXTRA_ACTION_TYPE, actionType.name)
                }
                startActivity(intent)
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    override fun onInterrupt() {
        // Accessibility interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
