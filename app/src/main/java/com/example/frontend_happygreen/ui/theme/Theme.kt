package com.example.frontend_happygreen.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    primary = VistaBlue,
    onPrimary = Color.White,
    primaryContainer = DelftBlue,
    onPrimaryContainer = LavenderBlush,
    secondary = Saffron,
    onSecondary = DelftBlue,
    secondaryContainer = TicklePink,
    onSecondaryContainer = DelftBlue,
    tertiary = TicklePink,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DelftBlue,
    onPrimary = Color.White,
    primaryContainer = LavenderBlush,
    onPrimaryContainer = DelftBlue,
    secondary = Saffron,
    onSecondary = DelftBlue,
    secondaryContainer = TicklePink.copy(alpha = 0.3f),
    onSecondaryContainer = DelftBlue,
    tertiary = TicklePink,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = DelftBlue,
    surface = Color.White,
    onSurface = DelftBlue,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = DelftBlue.copy(alpha = 0.8f),
    outline = DelftBlue.copy(alpha = 0.3f),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun FrontendhappygreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabilitato per usare i nostri colori
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}