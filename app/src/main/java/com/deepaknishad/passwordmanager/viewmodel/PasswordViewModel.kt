package com.deepaknishad.passwordmanager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deepaknishad.passwordmanager.data.PasswordDatabase
import com.deepaknishad.passwordmanager.data.PasswordEntity
import com.deepaknishad.passwordmanager.model.Password
import com.deepaknishad.passwordmanager.util.EncryptionUtil
import com.deepaknishad.passwordmanager.util.PasswordGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val passwordDao = PasswordDatabase.getDatabase(application).passwordDao()
    val passwords: Flow<List<Password>> = passwordDao.getAllPasswords().map { entities ->
        entities.map { entity ->
            Password(
                id = entity.id,
                accountType = entity.accountType,
                username = entity.username,
                password = getDecryptedPassword(entity)
            )
        }
    }

    private companion object {
        const val TAG = "PasswordViewModel"
    }

    init {
        Log.d(TAG, "ViewModel initialized")
    }

    suspend fun addPassword(accountType: String, username: String, password: String): Long {
        Log.d(TAG, "Starting to add password for accountType: $accountType")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Encrypting password...")
                val (encryptedPassword, iv) = EncryptionUtil.encrypt(password)
                Log.d(TAG, "Password encrypted successfully")

                val passwordEntity = PasswordEntity(
                    accountType = accountType,
                    username = username,
                    encryptedPassword = encryptedPassword,
                    iv = iv
                )
                Log.d(TAG, "Entity created with initial ID: ${passwordEntity.id}")

                Log.d(TAG, "Inserting into database...")
                val insertedId = passwordDao.insertPassword(passwordEntity)
                Log.d(TAG, "Insert completed with generated ID: $insertedId")

                insertedId
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add password: ${e.message}", e)
                throw e
            }
        }
    }

    fun updatePassword(
        password: Password, accountType: String, username: String, newPassword: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating password ID: ${password.id}")
                val (encryptedPassword, iv) = EncryptionUtil.encrypt(newPassword)
                val updatedEntity = PasswordEntity(
                    id = password.id,
                    accountType = accountType,
                    username = username,
                    encryptedPassword = encryptedPassword,
                    iv = iv
                )
                val rowsAffected = passwordDao.updatePassword(updatedEntity)
                Log.d(TAG, "Update completed, rows affected: $rowsAffected")
                if (rowsAffected == 0) {
                    Log.w(TAG, "No rows updated for ID: ${password.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update password: ${e.message}", e)
            }
        }
    }

    fun deletePassword(password: Password) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Deleting password ID: ${password.id}")
                val entity = PasswordEntity(
                    id = password.id,
                    accountType = password.accountType,
                    username = password.username,
                    encryptedPassword = "",
                    iv = ""
                )
                val rowsAffected = passwordDao.deletePassword(entity)
                Log.d(TAG, "Delete completed, rows affected: $rowsAffected")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete password: ${e.message}", e)
            }
        }
    }

    private fun getDecryptedPassword(passwordEntity: PasswordEntity): String {
        return try {
            Log.d(TAG, "Decrypting password ID: ${passwordEntity.id}")
            val decrypted =
                EncryptionUtil.decrypt(passwordEntity.encryptedPassword, passwordEntity.iv)
            Log.d(TAG, "Decryption successful for ID: ${passwordEntity.id}")
            decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed for ID: ${passwordEntity.id}", e)
            "Error decrypting password"
        }
    }

    fun generateRandomPassword(): String {
        Log.d(TAG, "Generating random password")
        return PasswordGenerator.generatePassword().also {
            Log.d(TAG, "Generated password: $it")
        }
    }
}