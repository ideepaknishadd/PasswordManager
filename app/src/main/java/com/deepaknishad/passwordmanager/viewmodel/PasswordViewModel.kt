package com.deepaknishad.passwordmanager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deepaknishad.passwordmanager.data.PasswordDatabase
import com.deepaknishad.passwordmanager.data.PasswordEntity
import com.deepaknishad.passwordmanager.util.EncryptionUtil
import com.deepaknishad.passwordmanager.util.PasswordGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val passwordDao = PasswordDatabase.getDatabase(application).passwordDao()
    val passwords: Flow<List<PasswordEntity>> = passwordDao.getAllPasswords()

    init {
        try {
            EncryptionUtil.deleteKey()
            EncryptionUtil.generateKey()
        } catch (e: Exception) {
            Log.e("PasswordViewModel", "Failed to generate key: ${e.message}")
        }
    }

    fun addPassword(accountType: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                val (encryptedData, iv) = EncryptionUtil.encrypt(password)
                if (encryptedData.isEmpty()) {
                    throw IllegalStateException("Encryption failed: Empty data")
                }
                val passwordEntity = PasswordEntity(
                    accountType = accountType,
                    username = username,
                    encryptedPassword = encryptedData,
                    iv = iv
                )
                passwordDao.insertPassword(passwordEntity)
                Log.d("PasswordViewModel", "Password inserted: $passwordEntity")
            } catch (e: Exception) {
                Log.e("PasswordViewModel", "Failed to add password: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
            }
        }
    }

    fun updatePassword(passwordEntity: PasswordEntity, newPassword: String) {
        viewModelScope.launch {
            try {
                val (encryptedData, iv) = EncryptionUtil.encrypt(newPassword)
                if (encryptedData.isEmpty()) {
                    throw IllegalStateException("Encryption failed: Empty data")
                }
                val updatedEntity = passwordEntity.copy(
                    encryptedPassword = encryptedData,
                    iv = iv
                )
                passwordDao.updatePassword(updatedEntity)
                Log.d("PasswordViewModel", "Password updated: $updatedEntity")
            } catch (e: Exception) {
                Log.e("PasswordViewModel", "Failed to update password: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
            }
        }
    }

    fun getDecryptedPassword(passwordEntity: PasswordEntity): String {
        return try {
            val decrypted = EncryptionUtil.decrypt(passwordEntity.encryptedPassword, passwordEntity.iv)
            Log.d("PasswordViewModel", "Decrypted password successfully")
            decrypted
        } catch (e: Exception) {
            Log.e("PasswordViewModel", "Failed to decrypt password: ${e.message}, Stacktrace: ${e.stackTraceToString()}")
            "Decryption failed"
        }
    }

    fun deletePassword(passwordEntity: PasswordEntity) {
        viewModelScope.launch {
            try {
                passwordDao.deletePassword(passwordEntity)
                Log.d("PasswordViewModel", "Password deleted: $passwordEntity")
            } catch (e: Exception) {
                Log.e("PasswordViewModel", "Failed to delete password: ${e.message}")
            }
        }
    }

    fun generateRandomPassword(): String {
        return PasswordGenerator.generatePassword()
    }
}