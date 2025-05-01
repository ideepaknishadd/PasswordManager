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
    var isCheckingBiometricSupport by remember { mutableStateOf(true) }
    var isBiometricEnabled by remember {
        mutableStateOf(runBlocking {
            val enabled =
                context.dataStore.data.first()[booleanPreferencesKey("biometric_enabled")] ?: true
            Log.d("MainActivity", "PasswordManagerApp: Biometric enabled from DataStore: $enabled")
            enabled
        })
    }
    var biometricSupportStatus by remember { mutableStateOf<String?>(null) }

    val biometricManager = BiometricManager.from(context)
    val canAuthenticateWithBiometrics =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MainActivity", "PasswordManagerApp: Biometric authentication available")
                true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                biometricSupportStatus = "Biometric authentication is not available on this device."
                Log.w("MainActivity", "PasswordManagerApp: No biometric hardware available")
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                biometricSupportStatus =
                    "No biometric credentials enrolled. Please set up biometrics in your device settings."
                Log.w("MainActivity", "PasswordManagerApp: No biometric credentials enrolled")
                false
            }

            else -> {
                biometricSupportStatus = "Unknown biometric error."
                Log.e("MainActivity", "PasswordManagerApp: Unknown biometric error")
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

                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        Log.w("MainActivity", "BiometricPrompt: No biometric credentials enrolled")
                        "No biometric credentials enrolled."
                    }

                    BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Log.w("MainActivity", "BiometricPrompt: Too many failed attempts")
                        "Too many failed attempts. Biometric authentication is locked."
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
        .setSubtitle("Use your biometric to access the app").setNegativeButtonText("Cancel").build()

    LaunchedEffect(authErrorMessage) {
        if (authErrorMessage == "Authentication canceled. App will close.") {
            Log.d(
                "MainActivity", "PasswordManagerApp: Closing app due to authentication cancellation"
            )
            activity.finish()
        }
    }

    LaunchedEffect(isCheckingBiometricSupport) {
        if (canAuthenticateWithBiometrics && isBiometricEnabled) {
            Log.d("MainActivity", "PasswordManagerApp: Prompting for biometric authentication")
            biometricPrompt.authenticate(promptInfo)
        } else if (!canAuthenticateWithBiometrics) {
            Log.d(
                "MainActivity",
                "PasswordManagerApp: Biometric not supported, proceeding without authentication"
            )
            isAuthenticated = true // Allow app to proceed without biometric
        }
        isCheckingBiometricSupport = false
        Log.d("MainActivity", "PasswordManagerApp: Finished checking biometric support")
    }

    if (isCheckingBiometricSupport) {
        Log.d("MainActivity", "PasswordManagerApp: Showing biometric support check UI")
        Text("Checking biometric support...", modifier = Modifier.padding(16.dp))
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

    if (isAuthenticated || !isBiometricEnabled || !canAuthenticateWithBiometrics) {
        Log.d(
            "MainActivity",
            "PasswordManagerApp: Rendering HomeScreen (authenticated: $isAuthenticated, biometricEnabled: $isBiometricEnabled)"
        )
        Column(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                isBiometricEnabled = isBiometricEnabled,
                biometricSupportStatus = biometricSupportStatus,
                canAuthenticateWithBiometrics = canAuthenticateWithBiometrics,
                onBiometricToggle = { enabled ->
                    Log.d(
                        "MainActivity", "PasswordManagerApp: Biometric toggle changed to: $enabled"
                    )
                    runBlocking {
                        context.dataStore.edit { settings ->
                            settings[booleanPreferencesKey("biometric_enabled")] = enabled
                        }
                    }
                    isBiometricEnabled = enabled
                    if (enabled && canAuthenticateWithBiometrics && !isAuthenticated) {
                        Log.d(
                            "MainActivity",
                            "PasswordManagerApp: Re-prompting for biometric authentication after toggle"
                        )
                        biometricPrompt.authenticate(promptInfo)
                    }
                },
                viewModel = viewModel
            )
        }
    }
}