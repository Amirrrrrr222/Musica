package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Cream & Soil Palette with State-Driven Theme Selection and custom variant expansions

val WarmCreamBg: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF000000)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE, AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFFFFF)
            else -> Color(0xFFECE5D9)
        }
    }
}

val LighterCream: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF161616)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFFF8F9FA)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFF0F3) // Soft cherry blossom background tint
            else -> Color(0xFFF4EFE6)
        }
    }
}

val DarkestCream: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF2C2C2C)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFFE9ECEF)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFCCD5)
            else -> Color(0xFFDDD2BF)
        }
    }
}

val SandBrown: Color get() {
    if (ThemeState.isDark) {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.ORANGE -> Color(0xFFFFB74D) // Lighter, prettier orange accent
            AppThemeVariant.RED -> Color(0xFFEF5350)    // Prettier light red accent
            else -> Color(0xFFE0E0E0)
        }
    } else {
         return when (ThemeState.themeVariant) {
             AppThemeVariant.BLUE -> Color(0xFF1A73E8) // Google logo blue
             AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFD03B68) // Soft pink crimson
             else -> Color(0xFF8E7E6B)
         }
    }
}

val DeepSoil: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFFFFFFFF)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFF212529)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFF49111C) // Elegant deep cherry wine
            else -> Color(0xFF332D25)
        }
    }
}

val MutedSoil: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF9E9E9E)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFF6C757D)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFF805259)
            else -> Color(0xFF736859)
        }
    }
}

val AccentAmber: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF262626)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFFE8F0FE)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFF0F3)
            else -> Color(0xFFC0B19F)
        }
    }
}

val AccentPressed: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF3C3C3C)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFFD2E3FC)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFD6E0)
            else -> Color(0xFFAB9B89)
        }
    }
}

val BottomBarBg: Color get() {
    if (ThemeState.isDark) {
        return Color(0xFF101010)
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFFF1F3F5)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFFFE5EC)
            else -> Color(0xFFD5C7B4)
        }
    }
}

val ActiveAccentColor: Color get() {
    if (ThemeState.isDark) {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.ORANGE -> Color(0xFFFF9800)
            AppThemeVariant.RED -> Color(0xFFE53935)
            else -> Color(0xFF1DB954) // Green for default dark
        }
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFF1A73E8)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFD03B68)
            else -> SandBrown // SandBrown for default cream
        }
    }
}

val ActiveHighlightBg: Color get() {
    if (ThemeState.isDark) {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.ORANGE -> Color(0xFFFF9800).copy(alpha = 0.15f)
            AppThemeVariant.RED -> Color(0xFFE53935).copy(alpha = 0.15f)
            else -> Color(0xFF1DB954).copy(alpha = 0.12f)
        }
    } else {
        return when (ThemeState.themeVariant) {
            AppThemeVariant.BLUE -> Color(0xFF1A73E8).copy(alpha = 0.12f)
            AppThemeVariant.CHERRY_BLOSSOM -> Color(0xFFD03B68).copy(alpha = 0.12f)
            else -> DarkestCream.copy(alpha = 0.35f)
        }
    }
}

