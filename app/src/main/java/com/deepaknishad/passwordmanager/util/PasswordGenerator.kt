package com.deepaknishad.passwordmanager.util

import android.util.Log
import kotlin.random.Random

object PasswordGenerator {
    private const val TAG = "PasswordGenerator"

    fun generatePassword(length: Int = 12): String {
        Log.d(TAG, "Generating password with length: $length")
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + "!@#$%^&*()"
        return (1..length).map { chars[Random.nextInt(chars.size)] }.joinToString("").also {
            Log.d(TAG, "Generated password: $it")
        }
    }
}