package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricGold,
    onPrimary = Color.Black,
    secondary = ElectricTeal,
    onSecondary = Color.Black,
    background = TheaterBlack,
    onBackground = TextPrimaryWhite,
    surface = GraphiteCard,
    onSurface = TextPrimaryWhite,
    surfaceVariant = GraphiteSubtle,
    onSurfaceVariant = TextSecondaryGrey,
    outline = BorderGrey
)

// Define a gorgeous light theme for completeness, though theater dark is our principal aesthetic
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    onPrimary = Color.White,
    secondary = Color(0xFF00ACC1),
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF546E7A),
    outline = Color(0xFFCFD8DC)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for immersive TV/IPTV stream experience
    dynamicColor: Boolean = false, // Disable dynamic colors by default so our custom electric cinematic look isn't washed out
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
