package com.example.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object AuthenticationManager {

    interface AuthCallback {
        fun onAuthenticationSuccess(authMethod: String)
        fun onAuthenticationFailure()
        fun onAuthenticationError(errorCode: Int, errString: CharSequence)
    }

    /**
     * Authenticates using the official Android BiometricPrompt with Device Credential fallback
     * (PIN, Pattern, or Password). Never prompts for or stores a custom password.
     */
    fun promptDeviceAuthentication(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        callback: AuthCallback
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val promptCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val method = when (result.authenticationType) {
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> "Biometric (Fingerprint/Face)"
                    BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> "Device Credential (PIN/Pattern/Password)"
                    else -> "Device Lock"
                }
                callback.onAuthenticationSuccess(method)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                callback.onAuthenticationFailure()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                callback.onAuthenticationError(errorCode, errString)
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, promptCallback)

        // Android 11+ (API 30+) supports combining BIOMETRIC_STRONG and DEVICE_CREDENTIAL.
        // For API 29 and below, DEVICE_CREDENTIAL cannot be combined with negative button text.
        val allowedAuthenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(allowedAuthenticators)

        val promptInfo = promptInfoBuilder.build()
        biometricPrompt.authenticate(promptInfo)
    }
}
