package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

enum class AppLanguage {
    ENGLISH, PERSIAN
}

enum class AppThemeSetting {
    LIGHT, DARK
}

enum class AppThemeVariant {
    DEFAULT, ORANGE, RED, BLUE, CHERRY_BLOSSOM
}

object ThemeState {
    var isDark by mutableStateOf(false)
    var appLanguage by mutableStateOf(AppLanguage.ENGLISH)
    var themeVariant by mutableStateOf(AppThemeVariant.DEFAULT)

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("musica_prefs", Context.MODE_PRIVATE)
        isDark = prefs.getBoolean("is_dark", false)
        
        val variantStr = prefs.getString("theme_variant", AppThemeVariant.DEFAULT.name) ?: AppThemeVariant.DEFAULT.name
        themeVariant = try {
            AppThemeVariant.valueOf(variantStr)
        } catch (e: Exception) {
            AppThemeVariant.DEFAULT
        }

        val langStr = prefs.getString("app_language", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        appLanguage = try {
            AppLanguage.valueOf(langStr)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun toggleDarkTheme(context: Context, dark: Boolean) {
        isDark = dark
        val prefs = context.getSharedPreferences("musica_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark", dark).apply()
        
        // Ensure active variant is valid for the selected dark/light mode
        if (dark) {
            if (themeVariant == AppThemeVariant.BLUE || themeVariant == AppThemeVariant.CHERRY_BLOSSOM) {
                setThemeVariant(context, AppThemeVariant.DEFAULT)
            }
        } else {
            if (themeVariant == AppThemeVariant.ORANGE || themeVariant == AppThemeVariant.RED) {
                setThemeVariant(context, AppThemeVariant.DEFAULT)
            }
        }
    }

    fun setThemeVariant(context: Context, variant: AppThemeVariant) {
        themeVariant = variant
        val prefs = context.getSharedPreferences("musica_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_variant", variant.name).apply()
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        appLanguage = language
        val prefs = context.getSharedPreferences("musica_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", language.name).apply()
    }
}

// Custom Typography configuration that dynamically switches to a premium Serif font in Persian mode
val AppFontFamily: FontFamily
    get() = if (ThemeState.appLanguage == AppLanguage.PERSIAN) FontFamily.Serif else FontFamily.SansSerif

object LocalStrings {
    val language: AppLanguage
        get() = ThemeState.appLanguage

    val searchPlaceholder: String
        get() = if (language == AppLanguage.PERSIAN) "جستجوی آهنگ، هنرمند..." else "Search tracks, artists..."

    val libraryTitle: String
        get() = if (language == AppLanguage.PERSIAN) "کتابخانه" else "Library"

    val favoritesLabel: String
        get() = if (language == AppLanguage.PERSIAN) "علاقه‌مندی‌ها" else "Favorites"

    val offlineFilesLabel: String
        get() = if (language == AppLanguage.PERSIAN) "فایل‌های آفلاین" else "Offline Files"

    val albumsLabel: String
        get() = if (language == AppLanguage.PERSIAN) "آلبوم‌ها" else "Albums"

    val offlineTracksTitle: String
        get() = if (language == AppLanguage.PERSIAN) "آهنگ‌های آفلاین" else "Offline Tracks"

    val sortByTitleName: String
        get() = if (language == AppLanguage.PERSIAN) "بر اساس: نام" else "Sort: Name"

    val sortByTitleDate: String
        get() = if (language == AppLanguage.PERSIAN) "بر اساس: تاریخ" else "Sort: Date"

    val libraryEmptyTitle: String
        get() = if (language == AppLanguage.PERSIAN) "لیست آهنگ‌ها خالی است" else "Tracklist is empty"

    val libraryEmptySubtitle: String
        get() = if (language == AppLanguage.PERSIAN) "برای شروع پخش، آهنگ‌های خود را اسکن نموده یا موسیقی دمو را در صفحه اصلی بارگذاری کنید." else "To start playing, scan your device tracks or load the demo music."

    val noSongsFound: String
        get() = if (language == AppLanguage.PERSIAN) "هیچ آهنگی یافت نشد" else "No tracks found"

    val noSongsSubtitle: String
        get() = if (language == AppLanguage.PERSIAN) "برای شروع موسیقی‌های دستگاه خود را اسکن کنید یا نمونه‌های دمو را بارگذاری نمایید." else "Scan your device storage or load demo songs to begin."

    val scanStorageBtn: String
        get() = if (language == AppLanguage.PERSIAN) "اسکن حافظه دستگاه" else "Scan Device Storage"

    val loadDemoBtn: String
        get() = if (language == AppLanguage.PERSIAN) "بارگذاری آهنگ‌های نمونه (دمو)" else "Load Demo Songs (Demo)"

    // Lyrics Section
    val lyricsTitle: String
        get() = if (language == AppLanguage.PERSIAN) "متن ترانه (لیریک)" else "Lyrics"

    val noLyricsFound: String
        get() = if (language == AppLanguage.PERSIAN) "متنی برای این آهنگ یافت نشد. اگر متن لیریک ترانه را دارید، می‌توانید در بخش زیر اضافه کنید:" else "No lyrics found for this track. If you have the lyrics, you can add them below:"

    val saveLyricsBtn: String
        get() = if (language == AppLanguage.PERSIAN) "ثبت و ذخیره متن" else "Save Lyrics"

    val editBtn: String
        get() = if (language == AppLanguage.PERSIAN) "ویرایش" else "Edit"

    val writeLyricsPlaceholder: String
        get() = if (language == AppLanguage.PERSIAN) "متن ترانه را اینجا بنویسید..." else "Write lyrics here..."

    val songsCountSuffix: String
        get() = if (language == AppLanguage.PERSIAN) " آهنگ" else " songs"

    val albumsCountValue: String
        get() = if (language == AppLanguage.PERSIAN) "۲ آلبوم" else "2 albums"

    val settingsTitle: String
        get() = if (language == AppLanguage.PERSIAN) "تنظیمات" else "Settings"

    val tagline: String
        get() = "Pure sound. Truly free."

    val languageHeader: String
        get() = if (language == AppLanguage.PERSIAN) "زبان" else "Language"

    val englishOption: String
        get() = "English"

    val persianOption: String
        get() = "Persian"

    val themeHeader: String
        get() = if (language == AppLanguage.PERSIAN) "پوسته" else "Theme"

    val lightThemeOption: String
        get() = if (language == AppLanguage.PERSIAN) "پوسته روشن" else "Light Theme"

    val darkThemeOption: String
        get() = if (language == AppLanguage.PERSIAN) "پوسته تاریک" else "Dark Theme"

    val audioHeader: String
        get() = if (language == AppLanguage.PERSIAN) "تنظیمات صوتی" else "Audio Settings"

    val audioSectionPlaceholder: String
        get() = if (language == AppLanguage.PERSIAN) "تنظیمات پیشرفته صوتی اعم از اکولایزر در روزرسانی‌های بعدی اضافه خواهد شد." else "Advanced equalizers, playback buffers, and output drivers ready for future updates."

    val versionInfo: String
        get() = if (language == AppLanguage.PERSIAN) "نسخه ۰.۰.۸۰ — ویرایش پیکسو 🎶" else "Ver. 0.0.80 — Pixo Edition 🎶"

    val playerTabTitle: String
        get() = if (language == AppLanguage.PERSIAN) "در حال پخش" else "Now Playing"

    val libraryTabTitle: String
        get() = if (language == AppLanguage.PERSIAN) "کتابخانه" else "Library"
}
