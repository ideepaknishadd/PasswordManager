package com.deepaknishad.passwordmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepaknishad.passwordmanager.data.PasswordEntity
import com.deepaknishad.passwordmanager.util.PasswordStrengthMeter
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel

@Composable
fun AddEditScreen(
    password: PasswordEntity? = null,
    onSave: () -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    var accountType by remember { mutableStateOf(password?.accountType ?: "") }
    var username by remember { mutableStateOf(password?.username ?: "") }
    var passwordInput by remember { mutableStateOf(if (password != null) viewModel.getDecryptedPassword(password) else "") }
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
                        viewModel.addPassword(accountType, username, passwordInput)
                    } else {
                        viewModel.updatePassword(password, passwordInput)
                    }
                    onSave()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New Account")
        }

        if (password != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.updatePassword(password, passwordInput); onSave() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.deletePassword(password); onSave() },
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