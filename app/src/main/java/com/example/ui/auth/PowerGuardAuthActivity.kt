package com.example.ui.auth

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEventType
import com.example.data.repository.SecurityRepository
import com.example.security.AuthenticationManager
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PowerGuardAuthActivity : FragmentActivity() {

    companion object {
        const val EXTRA_ACTION_TYPE = "extra_action_type"
        const val EXTRA_IS_SIMULATION = "extra_is_simulation"
    }

    private lateinit var repository: SecurityRepository
    private var actionType: SecurityEventType = SecurityEventType.POWER_OFF_ATTEMPT
    private var isSimulation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = SecurityRepository(this)

        // Protect screen capture and show on top of lock screen
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val actionName = intent.getStringExtra(EXTRA_ACTION_TYPE)
        actionType = try {
            if (actionName != null) SecurityEventType.valueOf(actionName) else SecurityEventType.POWER_OFF_ATTEMPT
        } catch (_: Exception) {
            SecurityEventType.POWER_OFF_ATTEMPT
        }
        isSimulation = intent.getBooleanExtra(EXTRA_IS_SIMULATION, false)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                PowerGuardAuthScreen(
                    actionType = actionType,
                    isSimulation = isSimulation,
                    onStartAuthentication = { onPromptRequested() },
                    onCancel = { handleCancel() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatically invoke biometric / device lock prompt on launch
        onPromptRequested()
    }

    private var isAuthenticating = false

    private fun onPromptRequested() {
        if (isAuthenticating) return
        isAuthenticating = true

        val actionTitle = if (actionType == SecurityEventType.RESTART_ATTEMPT) "Restart Device" else "Power Off Device"
        val subtitle = "Device Authentication Required"
        val desc = "Authenticate using your device's screen lock (PIN, Pattern, Password, or Biometrics) to continue."

        AuthenticationManager.promptDeviceAuthentication(
            activity = this,
            title = actionTitle,
            subtitle = subtitle,
            description = desc,
            callback = object : AuthenticationManager.AuthCallback {
                override fun onAuthenticationSuccess(authMethod: String) {
                    isAuthenticating = false
                    lifecycleScope.launch {
                        repository.recordSuccessfulAuth()
                        repository.logSecurityEvent(
                            eventType = actionType,
                            authStatus = AuthResultStatus.SUCCESS,
                            authMethod = authMethod,
                            details = "User successfully authenticated for ${actionTitle.lowercase()}",
                            isSimulated = isSimulation
                        )
                        delay(600)
                        setResult(RESULT_OK)
                        finish()
                    }
                }

                override fun onAuthenticationFailure() {
                    isAuthenticating = false
                    lifecycleScope.launch {
                        repository.logSecurityEvent(
                            eventType = actionType,
                            authStatus = AuthResultStatus.FAILED,
                            authMethod = "Device Lock",
                            details = "Authentication failed for ${actionTitle.lowercase()}",
                            isSimulated = isSimulation
                        )
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    isAuthenticating = false
                    lifecycleScope.launch {
                        repository.logSecurityEvent(
                            eventType = actionType,
                            authStatus = AuthResultStatus.CANCELLED,
                            authMethod = "Device Lock",
                            details = "Authentication dismissed ($errString) for ${actionTitle.lowercase()}",
                            isSimulated = isSimulation
                        )
                    }
                }
            }
        )
    }

    private fun handleCancel() {
        lifecycleScope.launch {
            repository.logSecurityEvent(
                eventType = actionType,
                authStatus = AuthResultStatus.CANCELLED,
                authMethod = "User Cancelled",
                details = "User cancelled device authentication prompt",
                isSimulated = isSimulation
            )
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}

@Composable
fun PowerGuardAuthScreen(
    actionType: SecurityEventType,
    isSimulation: Boolean,
    onStartAuthentication: () -> Unit,
    onCancel: () -> Unit
) {
    var authFailed by remember { mutableStateOf(false) }
    var authSuccess by remember { mutableStateOf(false) }

    val actionName = if (actionType == SecurityEventType.RESTART_ATTEMPT) "Restart" else "Power Off"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6090E17))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF233658), RoundedCornerShape(24.dp))
                .testTag("auth_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Shield Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (authFailed) AlertRedBg else Color(0xFF10192A))
                        .border(
                            2.dp,
                            if (authFailed) AlertRed else NeonCyan,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authFailed) Icons.Default.Cancel else Icons.Default.PowerSettingsNew,
                        contentDescription = "Power Guard Shield",
                        tint = if (authFailed) AlertRed else NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Device Authentication Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Authenticate to $actionName this device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                if (isSimulation) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔒 Safety Simulation Mode: Verifying Screen Lock Integration",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB300),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PowerGuard protects sensitive system power actions using your phone's existing screen lock method (PIN, Pattern, Password, or Biometrics).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Authentication Button
                Button(
                    onClick = onStartAuthentication,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_authenticate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberNavyDark
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authenticate with Device Lock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_cancel_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cancel $actionName",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
