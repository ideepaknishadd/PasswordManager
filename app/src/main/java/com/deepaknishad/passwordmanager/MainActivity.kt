package com.deepaknishad.passwordmanager

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import com.deepaknishad.passwordmanager.ui.screens.HomeScreen
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Starting MainActivity")
        setContent {
            MaterialTheme {
                PasswordManagerApp(activity = this@MainActivity)
            }
        }
    }
}

@Composable
fun PasswordManagerApp(activity: MainActivity) {
    val context = LocalContext.current
    val viewModel: PasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    Log.d("MainActivity", "PasswordManagerApp: Initializing with viewModel")

    var isAuthenticated by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingAuthSupport by remember { mutableStateOf(true) }
    var isDeviceAuthEnabled by remember {
        mutableStateOf(runBlocking {
            val enabled =
                context.dataStore.data.first()[booleanPreferencesKey("device_auth_enabled")] ?: true
            Log.d(
                "MainActivity",
                "PasswordManagerApp: Device auth enabled from DataStore: $enabled"
            )
            enabled
        })
    }
    var authSupportStatus by remember { mutableStateOf<String?>(null) }

    val biometricManager = BiometricManager.from(context)
    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val canAuthenticateWithDevice = when (biometricManager.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            Log.d("MainActivity", "PasswordManagerApp: Device authentication available")
            true
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            authSupportStatus = "Device does not support biometric or credential authentication."
            Log.w("MainActivity", "PasswordManagerApp: No authentication hardware available")
            false
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            authSupportStatus =
                "No biometric or device credentials enrolled. Please set up authentication in your device settings."
            Log.w("MainActivity", "PasswordManagerApp: No authentication credentials enrolled")
            false
        }

        else -> {
            authSupportStatus = "Unknown authentication error."
            Log.e("MainActivity", "PasswordManagerApp: Unknown authentication error")
            false
        }
    }

    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(
        activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d("MainActivity", "BiometricPrompt: Authentication succeeded")
                isAuthenticated = true
                authErrorMessage = null
                Toast.makeText(context, "Authentication successful", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authErrorMessage = when (errorCode) {
                    BiometricPrompt.ERROR_CANCELED, BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        Log.w("MainActivity", "BiometricPrompt: Authentication canceled by user")
                        "Authentication canceled. App will close."
                    }

                    BiometricPrompt.ERROR_NO_BIOMETRICS, BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                        Log.w(
                            "MainActivity",
                            "BiometricPrompt: No authentication credentials enrolled"
                        )
                        "No biometric or device credentials enrolled."
                    }

                    BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Log.w("MainActivity", "BiometricPrompt: Too many failed attempts")
                        "Too many failed attempts. Authentication is locked."
                    }

                    else -> {
                        Log.e("MainActivity", "BiometricPrompt: Authentication error: $errString")
                        "Authentication error: $errString"
                    }
                }
            }

            override fun onAuthenticationFailed() {
                Log.w("MainActivity", "BiometricPrompt: Authentication failed")
                authErrorMessage = "Authentication failed. Please try again."
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate")
        .setSubtitle("Use biometrics or device credentials (PIN, pattern, or password)")
        .setAllowedAuthenticators(authenticators).build()

    LaunchedEffect(authErrorMessage) {
        if (authErrorMessage == "Authentication canceled. App will close.") {
            Log.d(
                "MainActivity",
                "PasswordManagerApp: Closing app due to authentication cancellation"
            )
            activity.finish()
        }
    }

    LaunchedEffect(isCheckingAuthSupport) {
        if (canAuthenticateWithDevice && isDeviceAuthEnabled) {
            Log.d("MainActivity", "PasswordManagerApp: Prompting for device authentication")
            biometricPrompt.authenticate(promptInfo)
        } else if (!canAuthenticateWithDevice) {
            Log.d(
                "MainActivity",
                "PasswordManagerApp: Device authentication not supported, proceeding without authentication"
            )
            isAuthenticated = true
        }
        isCheckingAuthSupport = false
        Log.d("MainActivity", "PasswordManagerApp: Finished checking authentication support")
    }

    if (isCheckingAuthSupport) {
        Log.d("MainActivity", "PasswordManagerApp: Showing authentication support check UI")
        Text("Checking authentication support...", modifier = Modifier.padding(16.dp))
        return
    }

    if (authErrorMessage != null) {
        Log.w("MainActivity", "PasswordManagerApp: Showing error message: $authErrorMessage")
        Text(
            text = authErrorMessage ?: "Unknown error",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    if (isAuthenticated || !isDeviceAuthEnabled || !canAuthenticateWithDevice) {
        Log.d(
            "MainActivity",
            "PasswordManagerApp: Rendering HomeScreen (authenticated: $isAuthenticated, deviceAuthEnabled: $isDeviceAuthEnabled)"
        )
        Column(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                isDeviceAuthEnabled = isDeviceAuthEnabled,
                authSupportStatus = authSupportStatus,
                canAuthenticateWithDevice = canAuthenticateWithDevice,
                onDeviceAuthToggle = { enabled ->
                    Log.d(
                        "MainActivity",
                        "PasswordManagerApp: Device auth toggle changed to: $enabled"
                    )
                    runBlocking {
                        context.dataStore.edit { settings ->
                            settings[booleanPreferencesKey("device_auth_enabled")] = enabled
                        }
                    }
                    isDeviceAuthEnabled = enabled
                    if (enabled && canAuthenticateWithDevice && !isAuthenticated) {
                        Log.d(
                            "MainActivity",
                            "PasswordManagerApp: Re-prompting for device authentication after toggle"
                        )
                        biometricPrompt.authenticate(promptInfo)
                    }
                },
                viewModel = viewModel
            )
        }
    }
}