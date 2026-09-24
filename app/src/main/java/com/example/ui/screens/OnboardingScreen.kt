package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.BiometricStatus
import com.example.security.DeviceSecurityDetector
import com.example.security.SecurityAuditReport
import com.example.ui.theme.AlertRed
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

@Composable
fun OnboardingScreen(
    audit: SecurityAuditReport?,
    onFinishOnboarding: () -> Unit,
    onRefreshAudit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) } // 0: Welcome, 1: Step1 Lock, 2: Step2 Biometrics, 3: Step3 Capabilities, 4: Complete

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Indicator Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (currentStep == index) 20.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentStep == index) NeonCyan else Color(0xFF233658))
                )
            }
        }

        // Animated Content for Each Step
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                0 -> WelcomeStepContent()
                1 -> Step1LockCheckContent(audit = audit, onOpenSettings = {
                    context.startActivity(DeviceSecurityDetector.openSecuritySettingsIntent())
                }, onRefresh = onRefreshAudit)
                2 -> Step2BiometricCheckContent(audit = audit)
                3 -> Step3CapabilitiesContent(audit = audit, onOpenAccessibility = {
                    context.startActivity(DeviceSecurityDetector.openAccessibilitySettingsIntent())
                })
                else -> Step4CompleteContent()
            }
        }

        // Navigation Action Buttons at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0 && currentStep < 4) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back", color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        onFinishOnboarding()
                    }
                },
                modifier = Modifier
                    .weight(if (currentStep == 0 || currentStep == 4) 1f else 1.5f)
                    .height(52.dp)
                    .testTag("onboarding_next_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberNavyDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = when (currentStep) {
                        0 -> "Check Device Security"
                        3 -> "Enable Available Protection"
                        4 -> "Enter PowerGuard"
                        else -> "Next Step"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun WelcomeStepContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF10283E))
                .border(2.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome to PowerGuard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Protect your phone's power controls.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = NeonCyan,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PowerGuard uses your existing Android device security to authenticate sensitive power actions where Android permits third-party protection.\n\nNo separate PIN or password required. Works directly with your Fingerprint, Face, PIN, or Pattern.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun Step1LockCheckContent(
    audit: SecurityAuditReport?,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    val isSecure = audit?.hasSecureScreenLock == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Step 1 of 4", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Check Device Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isSecure) ShieldGreenBg else Color(0xFF381014)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSecure) Icons.Default.Check else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isSecure) ShieldGreen else AlertRed,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isSecure) ShieldGreen else AlertRed, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isSecure) "Secure Screen Lock Detected" else "Device Lock Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSecure) ShieldGreen else AlertRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSecure) {
                        "Your device has a secure credential configured (${audit?.screenLockTypeDescription}). PowerGuard will use this existing credential for authorization."
                    } else {
                        "Please configure a secure screen lock (PIN, Pattern, or Password) in Android Settings before continuing. PowerGuard cannot authenticate users without a device lock."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (!isSecure) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("Open Security Settings", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun Step2BiometricCheckContent(audit: SecurityAuditReport?) {
    val biometricAvailable = audit?.biometricStatus == BiometricStatus.STRONG_AVAILABLE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Step 2 of 4", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Check Biometric Authentication", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (biometricAvailable) ShieldGreenBg else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = if (biometricAvailable) ShieldGreen else NeonCyan,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (biometricAvailable) "Biometrics Ready" else "Biometrics Optional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (biometricAvailable) ShieldGreen else NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = audit?.biometricDescription ?: "Checking biometric support...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If biometrics are unavailable or canceled during shutdown, Android automatically falls back to your device PIN, pattern, or password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun Step3CapabilitiesContent(
    audit: SecurityAuditReport?,
    onOpenAccessibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Step 3 of 4", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Check Android Capabilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Why PowerGuard needs Accessibility access",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "PowerGuard uses this service to detect supported power-control interactions and display an authentication requirement. It does not read passwords, messages, banking information, or personal content.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (audit?.isAccessibilityActive == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ShieldGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Accessibility Service Active", color = ShieldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onOpenAccessibility,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberNavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Accessibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable PowerGuard Accessibility")
                    }
                }
            }
        }
    }
}

@Composable
fun Step4CompleteContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(ShieldGreenBg)
                .border(2.dp, ShieldGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ShieldGreen,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Protection Setup Complete",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "PowerGuard is now active and guarding your power controls.",
            style = MaterialTheme.typography.bodyMedium,
            color = ShieldGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberNavyBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Protected Controls:", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text("• Power Off: Screen Lock Required\n• Restart: Screen Lock Required\n• Tamper Audit: Local Security Log Active", style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 20.sp)
            }
        }
    }
}
