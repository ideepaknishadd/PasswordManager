package com.deepaknishad.passwordmanager

import android.content.Context
import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deepaknishad.passwordmanager.ui.screens.AddEditScreen
import com.deepaknishad.passwordmanager.ui.screens.HomeScreen
import com.deepaknishad.passwordmanager.ui.screens.PasswordDetailsScreen
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PasswordManagerApp(activity = this@MainActivity)
            }
        }
    }
}

@Composable
fun PasswordManagerApp(activity: MainActivity) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: PasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // State for biometric authentication and toggle
    var isAuthenticated by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingBiometricSupport by remember { mutableStateOf(true) }
    var isBiometricEnabled by remember {
        mutableStateOf(runBlocking {
            context.dataStore.data.first()[booleanPreferencesKey("biometric_enabled")] ?: true
        })
    }

    // Check if biometric hardware is available
    val biometricManager = BiometricManager.from(context)
    val canAuthenticateWithBiometrics =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                authErrorMessage = "Biometric authentication is not available on this device."
                false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                authErrorMessage =
                    "No biometric credentials enrolled. Please set up biometrics in your device settings."
                false
            }

            else -> {
                authErrorMessage = "Unknown biometric error."
                false
            }
        }

    // Biometric Authentication Setup
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(
        activity, // Use the passed MainActivity instance
        executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isAuthenticated = true
                authErrorMessage = null
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authErrorMessage = when (errorCode) {
                    BiometricPrompt.ERROR_CANCELED, BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        "Authentication canceled. App will close."
                    }

                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        "No biometric credentials enrolled."
                    }

                    BiometricPrompt.ERROR_LOCKOUT, BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        "Too many failed attempts. Biometric authentication is locked."
                    }

                    else -> "Authentication error: $errString"
                }
            }

            override fun onAuthenticationFailed() {
                authErrorMessage = "Authentication failed. Please try again."
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Authenticate")
        .setSubtitle("Use your biometric to access the app").setNegativeButtonText("Cancel").build()

    // Handle app closure on authentication cancel
    LaunchedEffect(authErrorMessage) {
        if (authErrorMessage == "Authentication canceled. App will close.") {
            activity.finish()
        }
    }

    // Trigger biometric authentication if enabled and supported
    LaunchedEffect(isCheckingBiometricSupport) {
        if (canAuthenticateWithBiometrics && isBiometricEnabled) {
            biometricPrompt.authenticate(promptInfo)
        }
        isCheckingBiometricSupport = false
    }

    // Show error message if authentication fails or biometric is not supported
    if (authErrorMessage != null) {
        Text(
            text = authErrorMessage ?: "Unknown error",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    if (isCheckingBiometricSupport) {
        Text("Checking biometric support...")
        return
    }

    // Navigation Host
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            if (isAuthenticated || !isBiometricEnabled) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        onAddClick = { navController.navigate("add") },
                        onDetailsClick = { password ->
                            try {
                                navController.navigate("details/${password.id}")
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Navigation error: Unable to open details.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        isBiometricEnabled = isBiometricEnabled,
                        onBiometricToggle = { enabled ->
                            runBlocking {
                                context.dataStore.edit { settings ->
                                    settings[booleanPreferencesKey("biometric_enabled")] = enabled
                                }
                            }
                            isBiometricEnabled = enabled
                            if (enabled && canAuthenticateWithBiometrics && !isAuthenticated) {
                                biometricPrompt.authenticate(promptInfo)
                            }
                        })
                }
            }
        }
        composable("add") {
            if (isAuthenticated || !isBiometricEnabled) {
                AddEditScreen(
                    password = null, onSave = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Navigation error: Unable to return to home.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("home")
                        }
                    })
            }
        }
        composable("details/{id}") { backStackEntry ->
            if (isAuthenticated || !isBiometricEnabled) {
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                val passwords by viewModel.passwords.collectAsState(initial = emptyList())
                val password = passwords.find { it.id == id }
                if (password != null) {
                    PasswordDetailsScreen(password = password, onEdit = {
                        try {
                            navController.navigate("edit/${password.id}")
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Navigation error: Unable to open edit screen.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, onDelete = {
                        try {
                            viewModel.deletePassword(password)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context, "Error deleting password: ${e.message}", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Text(
                        text = "Password not found",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context, "Password with ID $id not found.", Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                }
            }
        }
        composable("edit/{id}") { backStackEntry ->
            if (isAuthenticated || !isBiometricEnabled) {
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                val passwords by viewModel.passwords.collectAsState(initial = emptyList())
                val password = passwords.find { it.id == id }
                if (password != null) {
                    AddEditScreen(
                        password = password, onSave = {
                            try {
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Navigation error: Unable to return to details.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("details/$id")
                            }
                        })
                } else {
                    Text(
                        text = "Password not found",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context, "Password with ID $id not found.", Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}