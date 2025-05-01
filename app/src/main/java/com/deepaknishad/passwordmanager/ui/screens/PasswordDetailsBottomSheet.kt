package com.deepaknishad.passwordmanager.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepaknishad.passwordmanager.model.Password

@Composable
fun PasswordDetailsBottomSheet(
    password: Password, onDismiss: () -> Unit, onEdit: (Password) -> Unit, onDelete: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Log.d("PasswordDetailsBottomSheet", "Rendering for password ID: ${password.id}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Account Details", fontSize = 20.sp, fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Account Type", fontSize = 14.sp, color = Color.Gray)
            Text(password.accountType, fontSize = 16.sp)
            Text("Username/Email", fontSize = 14.sp, color = Color.Gray)
            Text(password.username, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Password", fontSize = 14.sp, color = Color.Gray)
                IconButton(onClick = {
                    Log.d(
                        "PasswordDetailsBottomSheet",
                        "Password visibility toggled: ${!passwordVisible}"
                    )
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            }
            Text(
                text = if (passwordVisible) password.password else "****", fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    Log.d(
                        "PasswordDetailsBottomSheet", "Edit button clicked for ID: ${password.id}"
                    )
                    onEdit(password)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Edit", color = Color.White, fontSize = 16.sp)
            }
            Button(
                onClick = {
                    Log.d(
                        "PasswordDetailsBottomSheet", "Delete button clicked for ID: ${password.id}"
                    )
                    onDelete()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White, fontSize = 16.sp)
            }
        }

        Button(
            onClick = {
                Log.d("PasswordDetailsBottomSheet", "Close button clicked")
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Close", color = Color.White, fontSize = 16.sp)
        }
    }
}