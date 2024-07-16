package com.example.biometricsampleapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricUtils(
    private val context: Context,
) {
    fun checkBiometricAvailability(): BiometricAvailability {
        val packageManager = context.packageManager
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return BiometricAvailability.FINGERPRINT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)) {
                return BiometricAvailability.FACE_ONLY
            }
        }
        return BiometricAvailability.NOT_AVAILABLE
    }

    fun navigateToEnroll(launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val enrollIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                }
            launcher.launch(enrollIntent)
        }
    }

    fun checkBiometricStatus(type: Int): BiometricStatus =
        when (BiometricManager.from(context).canAuthenticate(type)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricStatus.NOT_ENROLLED
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                BiometricStatus.NO_HARDWARE
            }

            BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricStatus.AVAILABLE
            }

            else -> {
                BiometricStatus.UNAVAILABLE
            }
        }

    fun createPromptInfo(
        title: String,
        subtitle: String,
        type: Int,
        negativeButtonText: String,
        description: String = "",
        isConfirmationRequired: Boolean = false,
    ): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo
            .Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(type)
            .setDescription(description)
            .setConfirmationRequired(isConfirmationRequired)
            .setNegativeButtonText(negativeButtonText)
            .build()
    
    fun createBiometricPromptCallback(
        activity: FragmentActivity,
        executor: Executor,
        onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
        onFailed: () -> Unit,
        onError: (errorCode: Int, message: CharSequence) -> Unit,
    ): BiometricPrompt {
        return BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }
            },
        )
    }
}

enum class BiometricAvailability {
    FINGERPRINT,
    FACE_ONLY,
    NOT_AVAILABLE,
}

enum class BiometricStatus {
    AVAILABLE,
    UNAVAILABLE,
    NOT_ENROLLED,
    NO_HARDWARE,
}
