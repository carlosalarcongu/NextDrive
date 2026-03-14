package com.carlosalarcongu.nextdrive.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de colores Dark Minimalista Vampírica
private val VampiricDgtColorScheme = darkColorScheme(
    primary = BloodRed,
    onPrimary = PureWhite,
    primaryContainer = Crimson,
    onPrimaryContainer = PureWhite,

    secondary = DgtYellow,
    onSecondary = DeepBlack,
    secondaryContainer = OutlineGray,
    onSecondaryContainer = PureWhite,

    tertiary = LightBlood,
    onTertiary = PureWhite,
    tertiaryContainer = CardGray,

    background = NightGray,
    onBackground = PureWhite,

    surface = DeepBlack,
    onSurface = PureWhite,
    surfaceVariant = CardGray,
    onSurfaceVariant = TextGray,

    outline = OutlineGray,
    error = LightBlood,
    onError = PureWhite
)

@Composable
fun NextDriveTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = VampiricDgtColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pinta la barra de estado de arriba y la de navegación de abajo del color de fondo
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes, // Inyectamos nuestras formas afiladas
        content = content
    )
}