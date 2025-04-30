package com.deepaknishad.passwordmanager.viewmodel

import android.app.Application
import android.util.Base64
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
        EncryptionUtil.generateKey()
    }

    fun addPassword(accountType: String, username: String, password: String) {
        viewModelScope.launch {
            val (encryptedData, iv) = EncryptionUtil.encrypt(password)
            val encryptedPassword = Base64.encodeToString(encryptedData, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val passwordEntity = PasswordEntity(
                accountType = accountType,
                username = username,
                encryptedPassword = encryptedPassword,
                iv = ivString
            )
            passwordDao.insertPassword(passwordEntity)
        }
    }

    fun updatePassword(passwordEntity: PasswordEntity, newPassword: String) {
        viewModelScope.launch {
            val (encryptedData, iv) = EncryptionUtil.encrypt(newPassword)
            val encryptedPassword = Base64.encodeToString(encryptedData, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val updatedEntity = passwordEntity.copy(
                encryptedPassword = encryptedPassword, iv = ivString
            )
            passwordDao.updatePassword(updatedEntity)
        }
    }

    fun deletePassword(passwordEntity: PasswordEntity) {
        viewModelScope.launch {
            passwordDao.deletePassword(passwordEntity)
        }
    }

    fun getDecryptedPassword(passwordEntity: PasswordEntity): String {
        val encryptedData = Base64.decode(passwordEntity.encryptedPassword, Base64.DEFAULT)
        val iv = Base64.decode(passwordEntity.iv, Base64.DEFAULT)
        return EncryptionUtil.decrypt(encryptedData, iv)
    }

    fun generateRandomPassword(): String {
        return PasswordGenerator.generatePassword()
    }
}