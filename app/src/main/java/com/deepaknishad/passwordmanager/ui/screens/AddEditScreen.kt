package com.deepaknishad.passwordmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepaknishad.passwordmanager.model.Password
import com.deepaknishad.passwordmanager.util.PasswordStrengthMeter
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun AddEditScreen(
    password: Password? = null,
    onSave: (Long?) -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    var accountType by remember { mutableStateOf(password?.accountType ?: "") }
    var username by remember { mutableStateOf(password?.username ?: "") }
    var passwordInput by remember { mutableStateOf(password?.password ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (password == null) "Add New Account" else "Edit Account",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = accountType,
            onValueChange = { accountType = it },
            label = { Text("Account Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username/Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        PasswordStrengthMeter(password = passwordInput)

        Button(
            onClick = {
                if (accountType.isBlank() || username.isBlank() || passwordInput.isBlank()) {
                    errorMessage = "All fields are required"
                } else {
                    if (password == null) {
                        viewModel.viewModelScope.launch {
                            try {
                                val insertedId = viewModel.addPassword(accountType, username, passwordInput)
                                onSave(insertedId)
                            } catch (e: Exception) {
                                errorMessage = "Failed to add password: ${e.message}"
                            }
                        }
                    } else {
                        viewModel.updatePassword(password, passwordInput)
                        onSave(null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (password == null) "Add New Account" else "Save")
        }

        if (password != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.updatePassword(password, passwordInput); onSave(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.deletePassword(password); onSave(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { passwordInput = viewModel.generateRandomPassword() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Random Password")
        }
    }
}