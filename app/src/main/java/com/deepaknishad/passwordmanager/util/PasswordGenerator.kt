package com.deepaknishad.passwordmanager.util

import kotlin.random.Random

object PasswordGenerator {
    fun generatePassword(length: Int = 12): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + "!@#$%^&*()"
        return (1..length).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }
}