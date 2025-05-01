package com.deepaknishad.passwordmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountType: String,
    val username: String,
    val encryptedPassword: String,
    val iv: String
) {
    companion object {
        private const val TAG = "PasswordEntity"
    }

    init {
        android.util.Log.d(TAG, "Created PasswordEntity with initial ID: $id")
    }
}