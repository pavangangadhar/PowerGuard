package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.DeviceAdminSecurityReceiver
import com.example.security.DeviceSecurityDetector
import com.example.ui.components.SectionHeader
import com.example.ui.components.SecurityInfoRow
import com.example.ui.components.SecuritySwitchCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PowerGuardUiState

@Composable
fun ProtectionSettingsScreen(
    state: PowerGuardUiState,
    onTogglePowerOff: (Boolean) -> Unit,
    onToggleRestart: (Boolean) -> Unit,
    onToggleBoot: (Boolean) -> Unit,
    onToggleOnlyWhenLocked: (Boolean) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audit = state.auditReport
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Protection Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Configure security triggers and authentication preferences",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Power Protection Switches
        SectionHeader(
            title = "Power Protection Triggers",
            subtitle = "Choose which actions require screen lock authentication"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SecuritySwitchCard(
            title = "Require Authentication Before Power Off",
            subtitle = "Challenges user before shutdown dialog proceeds",
            icon = Icons.Default.PowerSettingsNew,
            checked = state.powerOffProtectionEnabled,
            onCheckedChange = onTogglePowerOff,
            testTag = "settings_power_off_switch"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecuritySwitchCard(
            title = "Require Authentication Before Restart",
            subtitle = "Challenges user before restart proceeds",
            icon = Icons.Default.Refresh,
            checked = state.restartProtectionEnabled,
            onCheckedChange = onToggleRestart,
            testTag = "settings_restart_switch"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecuritySwitchCard(
            title = "Require Authentication After Boot",
            subtitle = "Audits boot event and validates screen lock on startup",
            icon = Icons.Default.Security,
            checked = state.bootProtectionEnabled,
            onCheckedChange = onToggleBoot,
            testTag = "settings_boot_switch"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SecuritySwitchCard(
            title = "Protect Only When Device Was Locked",
            subtitle = "Skips prompt if phone is already unlocked in your hand",
            icon = Icons.Default.Lock,
            checked = state.protectOnlyWhenLocked,
            onCheckedChange = onToggleOnlyWhenLocked,
            testTag = "settings_locked_only_switch"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Authentication & Feedback
        SectionHeader(
            title = "Authentication & Feedback",
            subtitle = "Android official credential parameters"
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
                    label = "Lock Mechanism",
                    value = audit?.screenLockTypeDescription ?: "PIN, Pattern, or Password",
                    valueColor = NeonCyan,
                    icon = Icons.Default.Lock
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                SecurityInfoRow(
                    label = "Biometrics Mode",
                    value = audit?.biometricDescription ?: "System Biometrics",
                    valueColor = TextPrimary,
                    icon = Icons.Default.Fingerprint
                )
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Haptic Vibration", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text("Vibrate on intercept and auth failure", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = state.hapticFeedbackEnabled,
                        onCheckedChange = onToggleHapticFeedback,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberNavyDark,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF10192A)
                        ),
                        modifier = Modifier.testTag("settings_haptic_toggle")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Enterprise & Device Administration (Section 9)
        SectionHeader(
            title = "Enterprise & Device Management",
            subtitle = "Elevated system policies (Device Administrator & Device Owner)"
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Device Administrator", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = if (audit?.isDeviceAdminActive == true) "Active Policy Provider" else "Inactive",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (audit?.isDeviceAdminActive == true) ShieldGreen else TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (audit?.isDeviceAdminActive == true) {
                                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                                val comp = ComponentName(context, DeviceAdminSecurityReceiver::class.java)
                                dpm?.removeActiveAdmin(comp)
                                Toast.makeText(context, "Device Admin removed", Toast.LENGTH_SHORT).show()
                            } else {
                                context.startActivity(DeviceSecurityDetector.openDeviceAdminSettingsIntent(context))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (audit?.isDeviceAdminActive == true) Color(0xFF2C1517) else Color(0xFF10283E),
                            contentColor = if (audit?.isDeviceAdminActive == true) AlertRed else NeonCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_device_admin_button")
                    ) {
                        Text(if (audit?.isDeviceAdminActive == true) "Revoke" else "Activate")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CyberNavyBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ADB Device Owner Provisioning (Kiosk/Enterprise)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "To grant PowerGuard native programmatic reboot control in dedicated corporate environments, provision via ADB before account setup:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val adbCmd = "adb shell dpm set-device-owner com.aistudio.powerguard.secxzq/.receiver.DeviceAdminSecurityReceiver"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberNavyDark)
                        .border(1.dp, Color(0xFF223652), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adbCmd,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCmd))
                                Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy command", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Anti-Tampering & Security Notice (Section 15)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anti-Tampering Notice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Uninstalling a third-party application on standard Android removes its accessibility intercept. Activating Device Administrator prevents accidental or unauthorized uninstallation without device credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
