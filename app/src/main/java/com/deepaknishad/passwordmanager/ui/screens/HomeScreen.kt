package com.deepaknishad.passwordmanager.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepaknishad.passwordmanager.model.Password
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onDetailsClick: (Password) -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    val passwords by viewModel.passwords.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Biometric Authentication")
                Switch(
                    checked = isBiometricEnabled,
                    onCheckedChange = onBiometricToggle
                )
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(passwords) { password ->
                    PasswordItem(
                        password = password,
                        onClick = { onDetailsClick(password) }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordItem(
    password: Password,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = {
            Log.d("PasswordItem", "Clicked password ID: ${password.id}")
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = password.accountType,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "****",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}