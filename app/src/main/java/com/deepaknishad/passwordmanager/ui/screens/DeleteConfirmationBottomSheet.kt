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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationBottomSheet(
    accountType: String, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    Log.d("DeleteConfirmationBottomSheet", "Rendering for account: $accountType")

    ModalBottomSheet(
        onDismissRequest = {
            Log.d("DeleteConfirmationBottomSheet", "Dismissed")
            onDismiss()
        }, sheetState = sheetState, containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Confirm Deletion", fontSize = 20.sp, fontWeight = FontWeight.Bold
            )

            Text(
                text = "Are you sure you want to delete the account \"$accountType\"? This action cannot be undone.",
                fontSize = 16.sp,
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        Log.d(
                            "DeleteConfirmationBottomSheet",
                            "Confirm deletion clicked for account: $accountType"
                        )
                        onConfirm()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Delete", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = {
                        Log.d("DeleteConfirmationBottomSheet", "Cancel deletion clicked")
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}