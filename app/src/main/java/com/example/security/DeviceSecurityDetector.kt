package com.example.security

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.biometric.BiometricManager
import com.example.receiver.DeviceAdminSecurityReceiver
import com.example.service.ShutdownAccessibilityService

data class SecurityAuditReport(
    val hasSecureScreenLock: Boolean,
    val screenLockTypeDescription: String,
    val biometricStatus: BiometricStatus,
    val biometricDescription: String,
    val isAccessibilityActive: Boolean,
    val isDeviceAdminActive: Boolean,
    val isDeviceOwnerActive: Boolean,
    val androidVersionName: String,
    val apiLevel: Int,
    val manufacturer: String,
    val deviceModel: String,
    val manufacturerGuidance: ManufacturerGuidance,
    val protectionSupportLevel: ProtectionSupportLevel
)

enum class BiometricStatus {
    STRONG_AVAILABLE,
    WEAK_AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    UNAVAILABLE
}

enum class ProtectionSupportLevel {
    FULLY_SUPPORTED_WITH_ACCESSIBILITY,
    NATIVE_OEM_SUPPORTED,
    DEVICE_OWNER_ENTERPRISE,
    LIMITED_BY_SYSTEM_SECURITY
}

data class ManufacturerGuidance(
    val oemName: String,
    val hasNativePowerLock: Boolean,
    val nativeFeatureName: String?,
    val instructionSummary: String,
    val hardwarePowerOverrideNote: String
)

object DeviceSecurityDetector {

    fun performAudit(context: Context): SecurityAuditReport {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val biometricManager = BiometricManager.from(context)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

        // 1. Detect Screen Lock
        val isSecure = keyguardManager?.isDeviceSecure ?: false
        val screenLockDesc = if (isSecure) {
            "Configured (PIN, Pattern, or Password)"
        } else {
            "None / Insecure (Swipe or None)"
        }

        // 2. Detect Biometrics
        val canAuthBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val biometricStatus = when (canAuthBiometric) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.STRONG_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }

        val biometricDesc = when (biometricStatus) {
            BiometricStatus.STRONG_AVAILABLE -> "Fingerprint / Face enrolled and ready"
            BiometricStatus.NONE_ENROLLED -> "Hardware present, but no biometrics enrolled"
            BiometricStatus.NO_HARDWARE -> "No biometric hardware detected on device"
            BiometricStatus.UNAVAILABLE -> "Biometric hardware currently unavailable"
            BiometricStatus.WEAK_AVAILABLE -> "Weak biometrics available"
        }

        // 3. Accessibility Service Status
        val isAccessibilityActive = isAccessibilityServiceEnabled(context)

        // 4. Device Admin & Device Owner
        val adminComponent = ComponentName(context, DeviceAdminSecurityReceiver::class.java)
        val isDeviceAdmin = dpm?.isAdminActive(adminComponent) ?: false
        val isDeviceOwner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dpm?.isDeviceOwnerApp(context.packageName) ?: false
        } else {
            false
        }

        // 5. Manufacturer guidance
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val guidance = getManufacturerGuidance(manufacturer)

        // 6. Overall Support Level
        val supportLevel = when {
            guidance.hasNativePowerLock -> ProtectionSupportLevel.NATIVE_OEM_SUPPORTED
            isDeviceOwner -> ProtectionSupportLevel.DEVICE_OWNER_ENTERPRISE
            isAccessibilityActive -> ProtectionSupportLevel.FULLY_SUPPORTED_WITH_ACCESSIBILITY
            else -> ProtectionSupportLevel.LIMITED_BY_SYSTEM_SECURITY
        }

        return SecurityAuditReport(
            hasSecureScreenLock = isSecure,
            screenLockTypeDescription = screenLockDesc,
            biometricStatus = biometricStatus,
            biometricDescription = biometricDesc,
            isAccessibilityActive = isAccessibilityActive,
            isDeviceAdminActive = isDeviceAdmin,
            isDeviceOwnerActive = isDeviceOwner,
            androidVersionName = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            manufacturer = manufacturer,
            deviceModel = model,
            manufacturerGuidance = guidance,
            protectionSupportLevel = supportLevel
        )
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = ShutdownAccessibilityService::class.java.name
        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name == expectedServiceName
        }
    }

    fun openSecuritySettingsIntent(): Intent {
        return Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun openAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun openDeviceAdminSettingsIntent(context: Context): Intent {
        val adminComponent = ComponentName(context, DeviceAdminSecurityReceiver::class.java)
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "PowerGuard requires Device Administrator access to assist in locking the device and enforcing security policies before unauthorized power actions."
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun getManufacturerGuidance(manufacturer: String): ManufacturerGuidance {
        val lower = manufacturer.lowercase()
        return when {
            lower.contains("samsung") -> ManufacturerGuidance(
                oemName = "Samsung (One UI / Knox)",
                hasNativePowerLock = true,
                nativeFeatureName = "Lock network and security",
                instructionSummary = "Samsung One UI includes native Knox protection: Go to Settings > Lock screen > Secure lock settings > Enable 'Lock network and security' (Require unlock to power off when locked). PowerGuard works alongside this for full app-level audit.",
                hardwarePowerOverrideNote = "Holding Volume Down + Power for 7-10 seconds initiates a hardware PMIC reset on Samsung devices, which is handled at the motherboard level."
            )
            lower.contains("xiaomi") || lower.contains("redmi") || lower.contains("poco") -> ManufacturerGuidance(
                oemName = "Xiaomi (HyperOS / MIUI)",
                hasNativePowerLock = false,
                nativeFeatureName = "Accessibility Power Intercept",
                instructionSummary = "Xiaomi devices require granting Autostart and Accessibility permissions to PowerGuard in HyperOS/MIUI Security settings.",
                hardwarePowerOverrideNote = "Holding the physical power button for 10 seconds invokes a hardware reset bypass in the chipset."
            )
            lower.contains("google") -> ManufacturerGuidance(
                oemName = "Google Pixel (Stock Android)",
                hasNativePowerLock = false,
                nativeFeatureName = "Accessibility + Device Admin",
                instructionSummary = "Stock Android relies on PowerGuard's Accessibility Service to detect Global Actions power menus. For Kiosk/Enterprise, Device Owner mode provides reboot control.",
                hardwarePowerOverrideNote = "Holding Power for 30 seconds triggers a Google Pixel hardware watchdog hard reboot."
            )
            lower.contains("oneplus") || lower.contains("oppo") || lower.contains("realme") -> ManufacturerGuidance(
                oemName = "OnePlus / Oppo / Realme (ColorOS / OxygenOS)",
                hasNativePowerLock = false,
                nativeFeatureName = "Accessibility Power Intercept",
                instructionSummary = "Enable PowerGuard Accessibility service and ensure battery optimization does not background-kill the service.",
                hardwarePowerOverrideNote = "Holding Volume Up + Power for 10 seconds is a hardware-enforced forced power off on ColorOS."
            )
            else -> ManufacturerGuidance(
                oemName = manufacturer,
                hasNativePowerLock = false,
                nativeFeatureName = "Accessibility Power Intercept",
                instructionSummary = "PowerGuard uses Android's Accessibility Service to intercept power dialogs on this device and authenticate before allowing shutdown.",
                hardwarePowerOverrideNote = "Hardware long-press resets (10-15 seconds) are executed directly by the processor power management IC."
            )
        }
    }
}
