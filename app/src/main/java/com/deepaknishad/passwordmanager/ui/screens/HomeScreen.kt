package com.deepaknishad.passwordmanager.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepaknishad.passwordmanager.R
import com.deepaknishad.passwordmanager.model.Password
import com.deepaknishad.passwordmanager.viewmodel.PasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isBiometricEnabled: Boolean,
    biometricSupportStatus: String?,
    canAuthenticateWithBiometrics: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    val passwords by viewModel.passwords.collectAsState(initial = emptyList())
    var selectedPassword by remember { mutableStateOf<Password?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showDetailsBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Log.d("HomeScreen", "Rendering HomeScreen with ${passwords.size} passwords")

    Scaffold(topBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Password Manager",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Biometric Authentication",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onBiometricToggle,
                        enabled = canAuthenticateWithBiometrics, // Disable switch if biometric not supported
                    )
                }
                if (!canAuthenticateWithBiometrics && biometricSupportStatus != null) {
                    Text(
                        text = biometricSupportStatus,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 1.dp, color = Color.Gray
            )
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                Log.d("HomeScreen", "FAB clicked: Opening AddEditBottomSheet for adding")
                selectedPassword = null // Reset selected password for adding
                isEditing = false
                showAddBottomSheet = true
            }, containerColor = Color(0xFF4A90E2), shape = CircleShape
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen height and width
                .padding(padding)
        ) {
            if (passwords.isEmpty()) {
                Log.d("HomeScreen", "No passwords to display")
                Text(
                    text = "No passwords saved yet",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center) // Center both vertically and horizontally
                )
            } else {
                Log.d("HomeScreen", "Displaying ${passwords.size} passwords in LazyColumn")
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp)
                ) {
                    itemsIndexed(passwords) { index, password ->
                        PasswordItem(
                            password = password, onClick = {
                                Log.d("HomeScreen", "Password item clicked: ID ${password.id}")
                                selectedPassword = password
                                showDetailsBottomSheet = true
                            }, modifier = if (index == passwords.size - 1) {
                                Modifier.padding(bottom = 100.dp) // Add bottom margin to the last item
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddBottomSheet) {
        Log.d(
            "HomeScreen",
            "Showing AddEditBottomSheet, isEditing: $isEditing, selectedPassword: ${selectedPassword?.id}"
        )
        ModalBottomSheet(
            onDismissRequest = {
                Log.d("HomeScreen", "AddEditBottomSheet dismissed")
                showAddBottomSheet = false
                selectedPassword = null
                isEditing = false
            }, sheetState = sheetState, containerColor = Color.White
        ) {
            AddEditBottomSheet(password = if (isEditing) selectedPassword else null, onDismiss = {
                Log.d("HomeScreen", "AddEditBottomSheet onDismiss called")
                showAddBottomSheet = false
                selectedPassword = null
                isEditing = false
            }, onSave = { updatedPassword ->
                Log.d(
                    "HomeScreen",
                    "AddEditBottomSheet onSave called, updatedPassword: ${updatedPassword?.id}"
                )
                showAddBottomSheet = false
                selectedPassword = null
                isEditing = false
            })
        }
    }

    if (showDetailsBottomSheet && selectedPassword != null) {
        Log.d("HomeScreen", "Showing PasswordDetailsBottomSheet for ID ${selectedPassword?.id}")
        ModalBottomSheet(
            onDismissRequest = {
                Log.d("HomeScreen", "PasswordDetailsBottomSheet dismissed")
                showDetailsBottomSheet = false
                selectedPassword = null
            }, sheetState = sheetState, containerColor = Color.White
        ) {
            PasswordDetailsBottomSheet(password = selectedPassword!!, onDismiss = {
                Log.d("HomeScreen", "PasswordDetailsBottomSheet onDismiss called")
                showDetailsBottomSheet = false
                selectedPassword = null
            }, onEdit = { passwordToEdit ->
                Log.d(
                    "HomeScreen",
                    "PasswordDetailsBottomSheet onEdit called for ID ${passwordToEdit.id}"
                )
                selectedPassword = passwordToEdit
                isEditing = true
                showDetailsBottomSheet = false
                showAddBottomSheet = true
            }, onDelete = {
                Log.d(
                    "HomeScreen",
                    "PasswordDetailsBottomSheet onDelete called for ID ${selectedPassword?.id}"
                )
                showDetailsBottomSheet = false
                showDeleteConfirmation = true
            })
        }
    }

    if (showDeleteConfirmation && selectedPassword != null) {
        Log.d("HomeScreen", "Showing DeleteConfirmationBottomSheet for ID ${selectedPassword?.id}")
        DeleteConfirmationBottomSheet(accountType = selectedPassword!!.accountType, onConfirm = {
            Log.d("HomeScreen", "Delete confirmed for ID ${selectedPassword?.id}")
            viewModel.deletePassword(selectedPassword!!)
            showDeleteConfirmation = false
            selectedPassword = null
        }, onDismiss = {
            Log.d("HomeScreen", "Delete confirmation dismissed")
            showDeleteConfirmation = false
            selectedPassword = null
        })
    }
}

@Composable
fun PasswordItem(password: Password, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                Log.d("PasswordItem", "Clicked password ID: ${password.id}")
                onClick()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = password.accountType,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "****", fontSize = 14.sp, color = Color.Gray
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "View Details",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color.Gray)
            )
        }
    }
}