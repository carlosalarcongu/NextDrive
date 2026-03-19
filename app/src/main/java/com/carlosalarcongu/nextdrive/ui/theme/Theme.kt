package com.carlosalarcongu.nextdrive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// PALETA VAMPÍRICA
private val VampiricDark = darkColorScheme(
    primary = BloodRed, onPrimary = PureWhite, secondary = DgtYellow,
    background = NightGray, surface = DeepBlack, surfaceVariant = CardGray, error = LightBlood
)
private val VampiricLight = lightColorScheme(
    primary = BloodRed, onPrimary = PureWhite, secondary = DgtYellow,
    background = Color(0xFFFAFAFA), surface = PureWhite, surfaceVariant = Color(0xFFE0E0E0), error = Crimson
)

// PALETA FRUTAL
private val FruityDark = darkColorScheme(
    primary = GrapePurple, onPrimary = PureWhite, secondary = LimeGreen,
    background = FruitBgDark, surface = MonoDarkest, surfaceVariant = FruitCardDark, error = LightBlood
)
private val FruityLight = lightColorScheme(
    primary = LightGrape, onPrimary = PureWhite, secondary = LimeGreen,
    background = Color(0xFFF3E5F5), surface = PureWhite, surfaceVariant = Color(0xFFE1BEE7), error = Crimson
)

// PALETA MONOCROMÁTICA
private val MonoDarkScheme = darkColorScheme(
    primary = MonoLight, onPrimary = MonoDarkest, secondary = MonoLight,
    background = MonoDarkest, surface = MonoDark, surfaceVariant = MonoMedium, error = Color(0xFF757575)
)
private val MonoLightScheme = lightColorScheme(
    primary = MonoDarkest, onPrimary = MonoWhite, secondary = MonoDark,
    background = MonoWhite, surface = MonoLight, surfaceVariant = Color(0xFFBDBDBD), error = Color(0xFF616161)
)

@Composable
fun NextDriveTheme(
    themeMode: String = "SYSTEM",
    palette: String = "VAMPIRIC",
    fontSizeStr: String = "MEDIANO",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (palette) {
        "FRUTAL" -> if (darkTheme) FruityDark else FruityLight
        "MONOCROMÁTICO" -> if (darkTheme) MonoDarkScheme else MonoLightScheme
        else -> if (darkTheme) VampiricDark else VampiricLight // VAMPIRIC
    }

    // Escalado de fuentes
    val scale = when(fontSizeStr) {
        "PEQUEÑO" -> 0.85f
        "GRANDE" -> 1.15f
        else -> 1.0f // MEDIANO
    }

    val scaledTypography = Typography(
        headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * scale),
        titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * scale),
        titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * scale),
        bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * scale),
        bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * scale),
        labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * scale),
        labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * scale)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, shapes = Shapes, content = content)
}