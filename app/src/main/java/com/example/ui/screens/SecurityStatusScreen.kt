package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.security.BiometricStatus
import com.example.security.DeviceSecurityDetector
import com.example.security.ProtectionSupportLevel
import com.example.ui.components.SectionHeader
import com.example.ui.components.SecurityInfoRow
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.PowerGuardUiState

@Composable
fun SecurityStatusScreen(
    state: PowerGuardUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audit = state.auditReport
    val scrollState = rememberScrollState()

    val isLockSecure = audit?.hasSecureScreenLock == true
    val isPowerOffEnabled = state.powerOffProtectionEnabled
    val isRestartEnabled = state.restartProtectionEnabled

    val systemSupportText = when (audit?.protectionSupportLevel) {
        ProtectionSupportLevel.NATIVE_OEM_SUPPORTED -> "Native Supported"
        ProtectionSupportLevel.FULLY_SUPPORTED_WITH_ACCESSIBILITY -> "Supported"
        ProtectionSupportLevel.DEVICE_OWNER_ENTERPRISE -> "Enterprise Supported"
        else -> "Limited"
    }

    val systemSupportColor = when (audit?.protectionSupportLevel) {
        ProtectionSupportLevel.LIMITED_BY_SYSTEM_SECURITY -> WarningAmber
        else -> ShieldGreen
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = "Security Status",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Complete audit of device authentication and system power security",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Status Metrics Card (Section 12 specification)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(20.dp))
                .testTag("security_status_metrics_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Core Protection Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                SecurityInfoRow(
                    label = "Device Lock",
                    value = if (isLockSecure) "Secure" else "Insecure",
                    valueColor = if (isLockSecure) ShieldGreen else AlertRed,
                    isHighlighted = true,
                    icon = Icons.Default.Lock
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Authentication",
                    value = "Device Credential",
                    valueColor = NeonCyan,
                    isHighlighted = true,
                    icon = Icons.Default.Security
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Biometric",
                    value = if (audit?.biometricStatus == BiometricStatus.STRONG_AVAILABLE) "Available" else "Unavailable",
                    valueColor = if (audit?.biometricStatus == BiometricStatus.STRONG_AVAILABLE) ShieldGreen else TextSecondary,
                    isHighlighted = true,
                    icon = Icons.Default.Fingerprint
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Power-Off Protection",
                    value = if (isPowerOffEnabled) "Enabled" else "Disabled",
                    valueColor = if (isPowerOffEnabled) ShieldGreen else AlertRed,
                    isHighlighted = true,
                    icon = Icons.Default.PowerSettingsNew
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Restart Protection",
                    value = if (isRestartEnabled) "Enabled" else "Disabled",
                    valueColor = if (isRestartEnabled) ShieldGreen else AlertRed,
                    isHighlighted = true,
                    icon = Icons.Default.Refresh
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "System Support",
                    value = systemSupportText,
                    valueColor = systemSupportColor,
                    isHighlighted = true,
                    icon = Icons.Default.Shield
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Platform & Device Details
        SectionHeader(
            title = "Hardware & Platform Details",
            subtitle = "Android architecture and device capabilities"
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
                    label = "Device Model",
                    value = "${audit?.manufacturer} ${audit?.deviceModel}"
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Android Version",
                    value = "${audit?.androidVersionName} (API ${audit?.apiLevel})"
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Accessibility Intercept",
                    value = if (audit?.isAccessibilityActive == true) "Active" else "Inactive",
                    valueColor = if (audit?.isAccessibilityActive == true) ShieldGreen else WarningAmber
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Device Administrator",
                    value = if (audit?.isDeviceAdminActive == true) "Granted" else "Not Provisioned",
                    valueColor = if (audit?.isDeviceAdminActive == true) ShieldGreen else TextSecondary
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Device Owner (Enterprise)",
                    value = if (audit?.isDeviceOwnerActive == true) "Active" else "Normal App Mode",
                    valueColor = if (audit?.isDeviceOwnerActive == true) ShieldGreen else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Important Android System Security Limitations Card (Section 8 & 26)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(18.dp))
                .testTag("android_limitations_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A2D))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Android System Security Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transparency on Android Protection Mechanisms:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "1. Software Power Menu (Protected):\nWhen you press the power button, Android displays the on-screen Power Off & Restart dialog. PowerGuard intercepts this menu via Accessibility & Device Lock authentication before the action can proceed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "2. Hardware Force Reset (Silicon/Kernel Level):\n${audit?.manufacturerGuidance?.hardwarePowerOverrideNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "3. Enterprise Device Management:\nAndroid only allows direct programmatic shutdown via Device Owner (MDM/Kiosk) privileges. PowerGuard provides official device administration hooks without using unsafe root exploits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                if (audit?.manufacturerGuidance?.hasNativePowerLock == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A24)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ShieldGreen)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ Native ${audit.manufacturerGuidance.oemName} Feature Available:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ShieldGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = audit.manufacturerGuidance.instructionSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
