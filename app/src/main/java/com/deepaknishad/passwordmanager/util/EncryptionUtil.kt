package com.deepaknishad.passwordmanager.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object EncryptionUtil {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "PasswordManagerKey"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256
    private const val IV_LENGTH = 16

    @Throws(Exception::class)
    fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val builder = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(KEY_SIZE)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(Build.VERSION.SDK_INT >= 23)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else if (Build.VERSION.SDK_INT >= 23) {
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(-1)
            }

            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
            Log.d("EncryptionUtil", "Key generated using AndroidKeyStore")
        } catch (e: Exception) {
            Log.w("EncryptionUtil", "KeyStore failed, generating software key: ${e.message}")
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE)
            val secretKey = keyGenerator.generateKey()
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.setEntry(KEY_ALIAS, KeyStore.SecretKeyEntry(secretKey), null)
        }
    }

    @Throws(Exception::class)
    fun deleteKey() {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    @Throws(Exception::class)
    fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        if (secretKey == null) {
            throw IllegalStateException("Secret key is null after generation")
        }

        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        Log.d("EncryptionUtil", "Initializing cipher with key and IV")
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            Log.d("EncryptionUtil", "Cipher initialized successfully with CBC mode")
            val encryptedData = cipher.doFinal(data.toByteArray())
            if (encryptedData == null || encryptedData.isEmpty()) {
                throw IllegalStateException("Encryption produced null or empty data with CBC")
            }
            Log.d("EncryptionUtil", "Encryption successful with CBC, length: ${encryptedData.size}")
            return Pair(encryptedData, iv)
        } catch (e: Exception) {
            Log.e("EncryptionUtil", "CBC encryption failed: ${e.message}, Cause: ${e.cause}")
            // Fallback: Generate and store a new software key
            val fallbackKeyGen = KeyGenerator.getInstance("AES")
            fallbackKeyGen.init(KEY_SIZE)
            val fallbackKey = fallbackKeyGen.generateKey()
            keyStore.setEntry(KEY_ALIAS, KeyStore.SecretKeyEntry(fallbackKey), null)
            val fallbackCipher = Cipher.getInstance(TRANSFORMATION)
            fallbackCipher.init(Cipher.ENCRYPT_MODE, fallbackKey, ivSpec)
            val fallbackEncryptedData = fallbackCipher.doFinal(data.toByteArray())
            if (fallbackEncryptedData == null || fallbackEncryptedData.isEmpty()) {
                throw IllegalStateException("Fallback encryption failed")
            }
            Log.d("EncryptionUtil", "Fallback encryption successful, length: ${fallbackEncryptedData.size}")
            return Pair(fallbackEncryptedData, iv)
        }
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            throw IllegalStateException("Key not found in KeyStore")
        }
        var secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        if (secretKey == null) {
            Log.w("EncryptionUtil", "Secret key is null, regenerating")
            generateKey() // Regenerate key if null
            keyStore.load(null)
            secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            if (secretKey == null) {
                throw IllegalStateException("Failed to regenerate secret key")
            }
        }
        val ivSpec = IvParameterSpec(iv)
        Log.d("EncryptionUtil", "Initializing decryption cipher with key")
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            Log.d("EncryptionUtil", "Cipher initialized successfully for decryption")
            val decryptedData = cipher.doFinal(encryptedData)
            Log.d("EncryptionUtil", "Decryption successful with CBC")
            return String(decryptedData)
        } catch (e: Exception) {
            Log.e("EncryptionUtil", "CBC decryption failed: ${e.message}, Cause: ${e.cause}")
            throw e
        }
    }
}