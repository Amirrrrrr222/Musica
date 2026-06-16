package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActiveAccentColor
import com.example.ui.theme.ActiveHighlightBg
import com.example.ui.theme.AppFontFamily
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DarkestCream
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.LighterCream
import com.example.ui.theme.MutedSoil
import com.example.ui.theme.SandBrown
import com.example.ui.theme.ThemeState
import com.example.ui.theme.WarmCreamBg
import com.example.ui.theme.LocalStrings

@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreamBg)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Region (Header + Controls) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Navigation Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background((if (ThemeState.isDark) Color(0xFF141414) else LighterCream).copy(alpha = 0.70f))
                        .blur(20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (ThemeState.isDark) Color(0xFF1E1E1E) else LighterCream,
                                CircleShape
                            )
                            .testTag("settings_btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = DeepSoil
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = LocalStrings.settingsTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppFontFamily,
                        color = DeepSoil
                    )
                }
            }

            // 2. Language Selection Card Group
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(title = LocalStrings.languageHeader, icon = Icons.Default.Language)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LighterCream)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        LanguageSelectorItem(
                            title = LocalStrings.englishOption,
                            isSelected = ThemeState.appLanguage == AppLanguage.ENGLISH,
                            onClick = { ThemeState.setLanguage(context, AppLanguage.ENGLISH) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LanguageSelectorItem(
                            title = LocalStrings.persianOption,
                            isSelected = ThemeState.appLanguage == AppLanguage.PERSIAN,
                            onClick = { ThemeState.setLanguage(context, AppLanguage.PERSIAN) }
                        )
                    }
                }
            }

            // 3. Theme Selection Card Group
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(title = LocalStrings.themeHeader, icon = Icons.Default.ColorLens)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LighterCream)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        // Dark Mode first!
                        ThemeSelectorItem(
                            title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "حالت تاریک (Dark Mode)" else "Dark Mode",
                            isSelected = ThemeState.isDark,
                            onClick = { ThemeState.toggleDarkTheme(context, true) }
                        )
                        
                        if (ThemeState.isDark) {
                            Column(modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)) {
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "پیش‌فرض تاریک" else "Default Dark",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.DEFAULT,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.DEFAULT) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "تم نارنجی" else "Orange Theme",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.ORANGE,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.ORANGE) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "تم قرمز" else "Red Theme",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.RED,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.RED) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Light Mode second!
                        ThemeSelectorItem(
                            title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "حالت روشن (Light Mode)" else "Light Mode",
                            isSelected = !ThemeState.isDark,
                            onClick = { ThemeState.toggleDarkTheme(context, false) }
                        )
                        
                        if (!ThemeState.isDark) {
                            Column(modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)) {
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "پیش‌فرض روشن" else "Default Light",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.DEFAULT,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.DEFAULT) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "تم آبی" else "Blue Theme",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.BLUE,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.BLUE) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                VariantSelectorSubItem(
                                    title = if (ThemeState.appLanguage == AppLanguage.PERSIAN) "تم شکوفه گیلاس" else "Cherry Blossom Theme",
                                    isSelected = ThemeState.themeVariant == com.example.ui.theme.AppThemeVariant.CHERRY_BLOSSOM,
                                    onClick = { ThemeState.setThemeVariant(context, com.example.ui.theme.AppThemeVariant.CHERRY_BLOSSOM) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Audio Control Info Card Group
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsSectionHeader(title = LocalStrings.audioHeader, icon = Icons.Default.Audiotrack)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LighterCream)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = LocalStrings.audioHeader,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFontFamily,
                            color = DeepSoil
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LocalStrings.audioSectionPlaceholder,
                            fontSize = 12.sp,
                            fontFamily = AppFontFamily,
                            color = MutedSoil,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // --- Bottom Region (Slogan / Branding / Version Footer) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Musica",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = DeepSoil,
                letterSpacing = 1.sp
            )
            Text(
                text = "Pure sound. Truly free.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = MutedSoil,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MutedSoil,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ver. 0.0.80 — Pixo Edition 🎶",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = AppFontFamily,
                    color = MutedSoil
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SandBrown,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedSoil,
            letterSpacing = 0.5.sp,
            fontFamily = AppFontFamily
        )
    }
}

@Composable
fun LanguageSelectorItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) ActiveHighlightBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = AppFontFamily,
            color = if (isSelected) DeepSoil else MutedSoil
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = ActiveAccentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ThemeSelectorItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) ActiveHighlightBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = AppFontFamily,
            color = if (isSelected) DeepSoil else MutedSoil
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = ActiveAccentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun VariantSelectorSubItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ActiveHighlightBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        if (isSelected) ActiveAccentColor else MutedSoil.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .background(if (isSelected) ActiveAccentColor else Color.Transparent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = AppFontFamily,
                color = if (isSelected) DeepSoil else MutedSoil
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = ActiveAccentColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

