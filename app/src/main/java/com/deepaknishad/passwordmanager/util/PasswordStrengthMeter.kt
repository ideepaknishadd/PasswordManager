package com.deepaknishad.passwordmanager.util

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PasswordStrengthMeter(password: String) {
    val strength = calculatePasswordStrength(password)
    val color = when {
        strength <= 30 -> Color.Red
        strength <= 60 -> Color.Yellow
        else -> Color.Green
    }

    Log.d("PasswordStrengthMeter", "Password strength: $strength, color: $color")

    Box(
        modifier = Modifier
            .fillMaxWidth(fraction = strength / 100f)
            .height(8.dp)
            .background(color)
    )
}

fun calculatePasswordStrength(password: String): Int {
    var score = 0
    if (password.length >= 8) score += 30
    if (password.any { it.isDigit() }) score += 20
    if (password.any { it.isUpperCase() }) score += 20
    if (password.any { it.isLowerCase() }) score += 20
    if (password.any { "!@#$%^&*()".contains(it) }) score += 10
    Log.d("PasswordStrengthMeter", "Calculated password strength: $score")
    return score.coerceAtMost(100)
}