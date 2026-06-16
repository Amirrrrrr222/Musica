package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CreamColorScheme = lightColorScheme(
    primary = SandBrown,
    secondary = MutedSoil,
    tertiary = AccentAmber,
    background = WarmCreamBg,
    surface = BottomBarBg,
    onPrimary = WarmCreamBg,
    onSecondary = WarmCreamBg,
    onTertiary = DeepSoil,
    onBackground = DeepSoil,
    onSurface = DeepSoil,
    surfaceVariant = LighterCream,
    onSurfaceVariant = MutedSoil,
    outline = SandBrown
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Ignored as custom cream light-theme is forced by user request
    dynamicColor: Boolean = false, // Ignored to safeguard retro-organic color integrity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CreamColorScheme,
        typography = Typography,
        content = content
    )
}
