package com.deepaknishad.passwordmanager.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepaknishad.passwordmanager.model.Password
import com.deepaknishad.passwordmanager.util.PasswordStrengthMeter
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBottomSheet(
    password: Password? = null,
    onDismiss: () -> Unit,
    onSave: (Password?) -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    var accountType by remember { mutableStateOf(password?.accountType ?: "") }
    var username by remember { mutableStateOf(password?.username ?: "") }
    var passwordInput by remember { mutableStateOf(password?.password ?: "") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Log whether we're in add or edit mode
    Log.d("AddEditBottomSheet", "Rendering for password ID: ${password?.id ?: "new"}, accountType: ${password?.accountType}, username: ${password?.username}, password: ${password?.password}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (password == null) "Add New Account" else "Edit Account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = accountType,
            onValueChange = { accountType = it },
            label = { Text("Account Name") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp)),
            singleLine = true
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username/Email") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp)),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp)),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = {
                    Log.d("AddEditBottomSheet", "Password visibility toggled: ${!passwordVisible}")
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true
        )
        PasswordStrengthMeter(password = passwordInput)

        if (errorMessage.isNotEmpty()) {
            Log.w("AddEditBottomSheet", "Displaying error: $errorMessage")
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
        }

        // Vertically arranged buttons with distinct colors
        Button(
            onClick = {
                Log.d("AddEditBottomSheet", "Generate Random Password clicked")
                passwordInput = viewModel.generateRandomPassword()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Generate Random Password", color = Color.White, fontSize = 16.sp)
        }

        Button(
            onClick = {
                Log.d("AddEditBottomSheet", "Save button clicked, isEdit: ${password != null}")
                if (accountType.isBlank() || username.isBlank() || passwordInput.isBlank()) {
                    errorMessage = "All fields are required"
                    Log.w("AddEditBottomSheet", "Validation failed: All fields are required")
                } else {
                    viewModel.viewModelScope.launch {
                        try {
                            val insertedId = if (password == null) {
                                Log.d("AddEditBottomSheet", "Adding new password")
                                viewModel.addPassword(accountType, username, passwordInput)
                            } else {
                                Log.d("AddEditBottomSheet", "Updating password ID: ${password.id}")
                                viewModel.updatePassword(password, accountType, username, passwordInput)
                                password.id
                            }
                            Log.d("AddEditBottomSheet", "Save successful, ID: $insertedId")
                            onSave(password?.copy(id = insertedId, accountType = accountType, username = username, password = passwordInput) ?: Password(id = insertedId, accountType = accountType, username = username, password = passwordInput))
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "Failed to save: ${e.message}"
                            Log.e("AddEditBottomSheet", "Save failed: ${e.message}", e)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2)) // Blue for Add/Save
        ) {
            Text(
                if (password == null) "Add New Account" else "Update",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Button(
            onClick = {
                Log.d("AddEditBottomSheet", "Cancel button clicked")
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Cancel", color = Color.White, fontSize = 16.sp)
        }
    }
}