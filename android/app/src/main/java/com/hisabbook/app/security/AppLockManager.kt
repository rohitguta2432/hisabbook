package com.hisabbook.app.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val combinedAuthenticators: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK
        }

    fun canAuthenticate(): AuthCapability {
        val bm = BiometricManager.from(ctx)
        return when (bm.canAuthenticate(combinedAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthCapability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthCapability.NotEnrolled
            else -> AuthCapability.Unavailable
        }
    }

    fun prompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("HisabBook khol")
            .setSubtitle("Apni dukaan ka hisab dekho")
            .setAllowedAuthenticators(combinedAuthenticators)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            builder.setNegativeButtonText("Cancel")
        }

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> onCancel()
                        else -> onError(errString.toString())
                    }
                }
            }
        )
        prompt.authenticate(builder.build())
    }
}

enum class AuthCapability { Available, NotEnrolled, NoHardware, Unavailable }
