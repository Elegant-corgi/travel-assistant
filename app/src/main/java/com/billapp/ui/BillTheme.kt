package com.billapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFF0B400),
    onPrimary = Color(0xFF352500),
    secondary = Color(0xFFFFD44D),
    onSecondary = Color(0xFF3B2E00),
    tertiary = Color(0xFF8E88F5),
    background = Color(0xFFF7F4FF),
    onBackground = Color(0xFF232129),
    surface = Color(0xFFFFFCF6),
    onSurface = Color(0xFF232129),
    surfaceVariant = Color(0xFFF1EEFB),
    onSurfaceVariant = Color(0xFF8C8799),
    error = Color(0xFFE45B5B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFD45A),
    onPrimary = Color(0xFF3A2D00),
    secondary = Color(0xFFF9CC52),
    onSecondary = Color(0xFF322600),
    tertiary = Color(0xFFB0ABFF),
    background = Color(0xFF17151D),
    onBackground = Color(0xFFF8F5FF),
    surface = Color(0xFF211F28),
    onSurface = Color(0xFFF8F5FF),
    surfaceVariant = Color(0xFF2D2A39),
    onSurfaceVariant = Color(0xFFC8C2D8),
    error = Color(0xFFFFB4AB),
)

@Composable
fun BillTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
