# PowerGuard – Device Power Security

**Protect your device from unauthorized shutdowns and restarts using your existing screen lock.**

PowerGuard is an Android security application designed to enforce device authentication before the phone can be powered off or restarted.

---

## 1. Core Security Concept

* **Zero Separate Passwords**: PowerGuard strictly utilizes the device's **existing lock credential** (PIN, Pattern, Password, Fingerprint, or Face). It never prompts you to create a secondary password.
* **Official Android Security APIs**: Uses `BiometricPrompt` with `BIOMETRIC_STRONG` and `DEVICE_CREDENTIAL`.
* **Zero Credential Storage**: PowerGuard never intercepts, stores, logs, or transmits your PIN, password, pattern, fingerprint, or biometric data.

---

## 2. Architecture & Components

```
UI Layer (Jetpack Compose, M3)
       │
ViewModel (PowerGuardViewModel & StateFlow)
       │
Security Manager & Detector (DeviceSecurityDetector, AuthenticationManager)
       │
Official Android APIs (KeyguardManager, BiometricPrompt, DevicePolicyManager)
       │
Background Services (ShutdownAccessibilityService, DeviceAdminSecurityReceiver, BootReceiver)
       │
Persistence (Room Database for Security Events, DataStore for Preferences)
```

### Components
1. **`ShutdownAccessibilityService`**:
   - Detects the appearance of system global action dialogs (Power Off / Restart).
   - Automatically intercepts the dialog when protection is active and launches the secure authentication window (`PowerGuardAuthActivity`).
2. **`PowerGuardAuthActivity`**:
   - Secured with `WindowManager.LayoutParams.FLAG_SECURE` to prevent screenshots and screen recordings.
   - Configured with `showWhenLocked="true"` and `turnScreenOn="true"` to display directly over the lockscreen and power menu.
   - Automatically triggers `BiometricPrompt` with `DEVICE_CREDENTIAL` fallback.
3. **`DeviceAdminSecurityReceiver`**:
   - Provides enterprise Device Administration integration to prevent unauthorized uninstallation and enforce security policies.
   - For dedicated enterprise/kiosk deployments, supports Device Owner provisioning via ADB:
     ```sh
     adb shell dpm set-device-owner com.aistudio.powerguard.secxzq/.receiver.DeviceAdminSecurityReceiver
     ```
4. **`BootReceiver`**:
   - Listens for `ACTION_BOOT_COMPLETED` to audit restarts and enforce screen lock verification on startup.
5. **`SecurityEventDao` & `PowerGuardDatabase` (Room)**:
   - Maintains a local audit log of all shutdown attempts, authorization successes, and security configuration changes.

---

## 3. Real Android Security Analysis & Platform Boundaries

As mandated by Android security principles:

### A. Fully Supported Functionality (Third-Party App Mode)
* Device screen lock detection (`KeyguardManager.isDeviceSecure()`).
* Official Android device authentication (`BiometricPrompt` with `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`).
* Detection and interception of software power dialogs via Android Accessibility Service.
* Anti-tampering through Device Administration (`DevicePolicyManager`).
* Immutable local security event audit logging (Room).

### B. Enterprise / Device Owner Functionality
* Programmatic reboot execution (`DevicePolicyManager.reboot(admin)`).
* Lock Task Mode / Kiosk power menu restrictions (`LOCK_TASK_FEATURE_GLOBAL_ACTIONS`).

### C. Functionality Blocked by Android OS & Hardware Design
* **Hardware PMIC Long-Press Reset**: When a physical power button is held for 10–15 seconds, the Power Management Integrated Circuit (PMIC) and processor hardware watchdog trigger a hard power cutoff directly at the motherboard/silicon level. No Android software (neither third-party apps, nor Google Play Services, nor the Linux kernel user-space) can override physical PMIC triggers.
* **Direct System Shutdown without Device Owner**: Android's `android.permission.SHUTDOWN` and `android.permission.REBOOT` are `signatureOrSystem` permissions reserved strictly for platform-signed system software or provisioned Device Owners.

### D. Manufacturer-Specific Guidance
* **Samsung (One UI / Knox)**: Samsung devices feature built-in hardware Knox security. Users are guided to enable **Settings > Lock screen > Secure lock settings > Lock network and security** (Require unlock to power off when locked). PowerGuard functions alongside this to provide comprehensive event logging and device auditing.
* **Google Pixel (Stock Android)**: Uses PowerGuard's Accessibility Intercept for software power dialogs.
* **Xiaomi / HyperOS / MIUI**: Requires granting Autostart and Accessibility permissions in HyperOS Security settings.
* **OnePlus / Oppo / Realme (ColorOS)**: Requires enabling PowerGuard Accessibility service and exempting from aggressive background power saving.

---

## 4. Testing Checklist

- [x] **Authentication**:
  - [x] Biometric success (fingerprint/face)
  - [x] Device credential fallback (PIN, Pattern, Password)
  - [x] Authentication cancellation handling
  - [x] Failed authentication attempts logged to Room DB
- [x] **Power Controls**:
  - [x] Power-off protection toggle (ON/OFF)
  - [x] Restart protection toggle (ON/OFF)
  - [x] Interactive sandbox simulation in dashboard
- [x] **Device States**:
  - [x] Secure lock detection
  - [x] Insecure lock detection with warning and intent to Android Settings
  - [x] Biometric hardware detection
- [x] **Accessibility Intercept**:
  - [x] Global actions dialog detection
  - [x] Dialog dismissal and secure auth activity launch
- [x] **Audit Log**:
  - [x] Event insertion with timestamp and method
  - [x] Filter by All / Successful / Failed
  - [x] Clear log with confirmation dialog
