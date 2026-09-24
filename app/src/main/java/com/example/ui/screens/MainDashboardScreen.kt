package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEventType
import com.example.security.BiometricStatus
import com.example.security.DeviceSecurityDetector
import com.example.ui.components.SectionHeader
import com.example.ui.components.SecurityInfoRow
import com.example.ui.components.SecuritySwitchCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.PowerGuardUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainDashboardScreen(
    state: PowerGuardUiState,
    onTogglePowerOff: (Boolean) -> Unit,
    onToggleRestart: (Boolean) -> Unit,
    onSimulateAction: (SecurityEventType) -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onRefreshAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audit = state.auditReport
    val isOverallActive = (state.powerOffProtectionEnabled || state.restartProtectionEnabled) &&
            (audit?.hasSecureScreenLock == true)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title & Tagline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PowerGuard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Protect your device from unauthorized shutdowns",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onRefreshAudit,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberNavySurface)
                    .testTag("refresh_audit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Device Status",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Security Status Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isOverallActive) Color(0xFF1B5E20) else CyberNavyBorder,
                    RoundedCornerShape(24.dp)
                )
                .testTag("security_status_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOverallActive) Color(0xFF0C1F16) else CyberNavyCard
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isOverallActive) ShieldGreenBg else Color(0xFF1B2233))
                        .border(
                            2.dp,
                            if (isOverallActive) ShieldGreen else TextMuted,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Protection",
                        tint = if (isOverallActive) ShieldGreen else AlertRed,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isOverallActive) "🟢 Protection Enabled" else "🔴 Protection Disabled",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverallActive) ShieldGreen else AlertRed
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isOverallActive) {
                        "Sensitive power actions require your phone's existing device lock authentication."
                    } else {
                        "Enable power protection and ensure a secure screen lock is active."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Lock Status Warning or Confirmation
        if (audit != null) {
            if (!audit.hasSecureScreenLock) {
                // Warning Card: No Secure Lock
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AlertRed, RoundedCornerShape(16.dp))
                        .testTag("no_screen_lock_warning_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertRedBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Device Lock Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please configure a secure screen lock in Android Settings before enabling PowerGuard. PowerGuard relies on Android's official device authentication APIs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                context.startActivity(DeviceSecurityDetector.openSecuritySettingsIntent())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            modifier = Modifier.testTag("open_security_settings_button")
                        ) {
                            Text("Open Security Settings", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Confirmation: Secure Lock Detected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1B5E20), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1E14))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ShieldGreenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ShieldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device authentication detected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ShieldGreen
                            )
                            Text(
                                text = audit.screenLockTypeDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Accessibility Interception Banner (if not yet granted)
            if (!audit.isAccessibilityActive && !audit.manufacturerGuidance.hasNativePowerLock) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WarningAmber, RoundedCornerShape(16.dp))
                        .testTag("accessibility_request_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Accessibility,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Accessibility Intercept Needed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "To detect when the system Power Off or Restart menu appears and enforce device authentication, PowerGuard requires Accessibility service permission.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                context.startActivity(DeviceSecurityDetector.openAccessibilitySettingsIntent())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            modifier = Modifier.testTag("enable_accessibility_button")
                        ) {
                            Text("Enable PowerGuard Service", color = CyberNavyDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Section: Main Power Protection Toggles
        SectionHeader(
            title = "Power Protection",
            subtitle = "Enforce device authentication before power actions"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Switch 1: Require Authentication Before Power Off
        SecuritySwitchCard(
            title = "Require Authentication Before Power Off",
            subtitle = "Prompts for device lock before shutdown",
            icon = Icons.Default.PowerSettingsNew,
            checked = state.powerOffProtectionEnabled,
            onCheckedChange = onTogglePowerOff,
            testTag = "require_auth_power_off_card"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Switch 2: Require Authentication Before Restart
        SecuritySwitchCard(
            title = "Require Authentication Before Restart",
            subtitle = "Prompts for device lock before restart",
            icon = Icons.Default.Refresh,
            checked = state.restartProtectionEnabled,
            onCheckedChange = onToggleRestart,
            testTag = "require_auth_restart_card"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Testing Sandbox
        SectionHeader(
            title = "Interactive Test Sandbox",
            subtitle = "Test your device authentication flow in safe mode"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Verify how PowerGuard challenges unauthorized shutdowns using your official screen lock (PIN, Pattern, Password, or Biometrics):",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSimulateAction(SecurityEventType.POWER_OFF_ATTEMPT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("test_power_off_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF132A46),
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Power Off", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSimulateAction(SecurityEventType.RESTART_ATTEMPT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("test_restart_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF132A46),
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Restart", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Security Summary Quick Card
        SectionHeader(
            title = "Device Security",
            subtitle = "Overview of hardware & platform security"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SecurityInfoRow(
                    label = "Device Lock",
                    value = if (audit?.hasSecureScreenLock == true) "Secure" else "None",
                    valueColor = if (audit?.hasSecureScreenLock == true) ShieldGreen else AlertRed,
                    isHighlighted = true,
                    icon = Icons.Default.Lock
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Biometric",
                    value = when (audit?.biometricStatus) {
                        BiometricStatus.STRONG_AVAILABLE -> "Available"
                        BiometricStatus.NONE_ENROLLED -> "Not Enrolled"
                        BiometricStatus.NO_HARDWARE -> "No Hardware"
                        else -> "Unavailable"
                    },
                    valueColor = if (audit?.biometricStatus == BiometricStatus.STRONG_AVAILABLE) ShieldGreen else TextSecondary,
                    icon = Icons.Default.Fingerprint
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Authentication",
                    value = "Device Credential",
                    valueColor = NeonCyan,
                    icon = Icons.Default.Security
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Security Status",
                    value = if (isOverallActive) "Protected" else "Unprotected",
                    valueColor = if (isOverallActive) ShieldGreen else AlertRed,
                    isHighlighted = true,
                    onClick = onNavigateToStatus
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Security Activity Link
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = onNavigateToLogs,
                        modifier = Modifier.testTag("view_all_logs_button")
                    ) {
                        Text("View All (${state.eventCount})", color = NeonCyan, fontSize = 12.sp)
                    }
                }

                if (state.recentEvents.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No security events recorded yet. Perform a test above to view audit entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    val latest = state.recentEvents.first()
                    val formatter = SimpleDateFormat("dd MMM yyyy – hh:mm a", Locale.getDefault())
                    val dateStr = formatter.format(Date(latest.timestamp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = if (latest.eventType == SecurityEventType.RESTART_ATTEMPT) "Restart authentication requested" else "Power-off authentication requested",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        StatusBadge(
                            text = if (latest.authStatus == AuthResultStatus.SUCCESS) "Successful" else "Blocked/Failed",
                            isActive = latest.authStatus == AuthResultStatus.SUCCESS
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
