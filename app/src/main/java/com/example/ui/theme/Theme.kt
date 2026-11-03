package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val TradingDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1700),
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldOnContainer,
    secondary = ProfitGreen,
    onSecondary = Color.Black,
    secondaryContainer = ProfitGreenContainer,
    onSecondaryContainer = ProfitGreenOnContainer,
    tertiary = LossRed,
    onTertiary = Color.White,
    tertiaryContainer = LossRedContainer,
    onTertiaryContainer = LossRedOnContainer,
    background = NavyDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = NavyDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = NavyDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = NavyCardBorder,
    outlineVariant = GrayBorder
)

private val TradingLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = Color(0xFFDC2626),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEE2E2),
    onTertiaryContainer = Color(0xFF991B1B),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun TradingJournalTheme(
    darkTheme: Boolean = true, // Default to professional luxury Dark mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TradingDarkColorScheme else TradingLightColorScheme

    // Enforce RTL layout direction across the entire app for full Arabic fidelity
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
