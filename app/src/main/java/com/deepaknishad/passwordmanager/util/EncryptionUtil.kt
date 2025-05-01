package com.deepaknishad.passwordmanager.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptionUtil {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "PasswordManagerKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val TAG = "EncryptionUtil"

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        Log.d(TAG, "Checking if key alias exists: $KEY_ALIAS")

        return if (keyStore.containsAlias(KEY_ALIAS)) {
            try {
                Log.d(TAG, "Key exists, retrieving key")
                keyStore.getKey(KEY_ALIAS, null) as SecretKey
            } catch (e: Exception) {
                Log.w(TAG, "Key retrieval failed, deleting and recreating key: ${e.message}")
                keyStore.deleteEntry(KEY_ALIAS)
                createNewKey()
            }
        } else {
            Log.d(TAG, "Key does not exist, creating new key")
            createNewKey()
        }
    }

    private fun createNewKey(): SecretKey {
        Log.d(TAG, "Creating new key")
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256)
            .setUserAuthenticationRequired(false).build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey().also {
            Log.d(TAG, "New key created successfully")
        }
    }

    fun encrypt(data: String): Pair<String, String> {
        return try {
            Log.d(TAG, "Encrypting data")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            Log.d(TAG, "Encryption successful")
            Pair(encryptedData, ivString)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}", e)
            throw EncryptionException("Encryption failed: ${e.message}", e)
        }
    }

    fun decrypt(encryptedData: String, iv: String): String {
        return try {
            Log.d(TAG, "Decrypting data")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateKey()
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(iv, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
            val decrypted = String(decryptedBytes, Charsets.UTF_8)
            Log.d(TAG, "Decryption successful")
            decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}", e)
            throw EncryptionException("Decryption failed: ${e.message}", e)
        }
    }
}

class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)