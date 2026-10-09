package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    secondary = CyberNeonGreen,
    onSecondary = Color.Black,
    tertiary = CyberPink,
    background = CyberBg,
    surface = CyberCard,
    onBackground = CyberTextPrimary,
    onSurface = CyberTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SlateDark,
    onPrimary = Color.White,
    secondary = NotesAmber,
    onSecondary = Color.White,
    tertiary = NotesTeal,
    background = PaperWhite,
    surface = PaperCard,
    onBackground = SlateDark,
    onSurface = SlateDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
