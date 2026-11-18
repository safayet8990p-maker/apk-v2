package com.example.ui.theme

import androidx.compose.runtime.Composable

/**
 * Instant bilingual dictionary (English & Bangla) so switching Language in Settings
 * updates every screen, badge, button, and TalkBack label in real time without restart.
 */
object AppStrings {
    @Composable
    fun tr(en: String, bn: String): String {
        return when (LocalAppLanguage.current) {
            AppLanguage.ENGLISH -> en
            AppLanguage.BANGLA -> bn
        }
    }
}

@Composable
fun tr(en: String, bn: String): String = AppStrings.tr(en, bn)
