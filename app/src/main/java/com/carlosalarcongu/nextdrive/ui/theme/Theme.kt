package com.carlosalarcongu.nextdrive.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VampiricDgtColorScheme = darkColorScheme(
    primary = BloodRed, onPrimary = PureWhite, primaryContainer = Crimson, onPrimaryContainer = PureWhite,
    secondary = DgtYellow, onSecondary = DeepBlack, secondaryContainer = OutlineGray, onSecondaryContainer = PureWhite,
    tertiary = LightBlood, onTertiary = PureWhite, tertiaryContainer = CardGray,
    background = NightGray, onBackground = PureWhite, surface = DeepBlack, onSurface = PureWhite,
    surfaceVariant = CardGray, onSurfaceVariant = TextGray, outline = OutlineGray, error = LightBlood, onError = PureWhite
)

private val LightDgtColorScheme = lightColorScheme(
    primary = BloodRed, onPrimary = PureWhite, primaryContainer = Color(0xFFFFCDD2), onPrimaryContainer = BloodRed,
    secondary = DgtYellow, onSecondary = DeepBlack, secondaryContainer = Color(0xFFEEEEEE), onSecondaryContainer = DeepBlack,
    tertiary = LightBlood, onTertiary = PureWhite, tertiaryContainer = Color(0xFFF5F5F5),
    background = Color(0xFFFAFAFA), onBackground = DeepBlack, surface = PureWhite, onSurface = DeepBlack,
    surfaceVariant = Color(0xFFE0E0E0), onSurfaceVariant = Color(0xFF424242), outline = Color(0xFFBDBDBD), error = Crimson, onError = PureWhite
)

@Composable
fun NextDriveTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) VampiricDgtColorScheme else LightDgtColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = Shapes, content = content)
}