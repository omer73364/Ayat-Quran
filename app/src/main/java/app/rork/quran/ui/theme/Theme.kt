package app.rork.quran.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class QuranColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val card: Color,
    val text: Color,
    val textMuted: Color,
    val textOnPrimary: Color,
    val border: Color,
    val primary: Color,
    val primaryLight: Color,
    val accent: Color,
    val error: Color,
    val isDark: Boolean,
)

val LightQuranColors = QuranColors(
    background     = Background,
    surface        = Surface,
    surfaceAlt     = SurfaceAlt,
    card           = CardLight,
    text           = TextPrimary,
    textMuted      = TextMuted,
    textOnPrimary  = White,
    border         = BorderLight,
    primary        = PrimaryGreen,
    primaryLight   = PrimaryGreenL,
    accent         = AccentGold,
    error          = ErrorRed,
    isDark         = false,
)

val DarkQuranColors = QuranColors(
    background     = BackgroundDark,
    surface        = SurfaceDark,
    surfaceAlt     = SurfaceAltDark,
    card           = CardDark,
    text           = TextPrimaryDark,
    textMuted      = TextMutedDark,
    textOnPrimary  = White,
    border         = BorderDark,
    primary        = PrimaryTeal,
    primaryLight   = PrimaryTealL,
    accent         = AccentGoldDark,
    error          = ErrorRedDark,
    isDark         = true,
)

val LocalQuranColors = staticCompositionLocalOf { LightQuranColors }

@Composable
fun QuranTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val quranColors = if (darkTheme) DarkQuranColors else LightQuranColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary   = PrimaryTeal,
            secondary = AccentGoldDark,
            background = BackgroundDark,
            surface   = SurfaceDark,
            error     = ErrorRedDark,
            onPrimary = White,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark,
        )
    } else {
        lightColorScheme(
            primary   = PrimaryGreen,
            secondary = AccentGold,
            background = Background,
            surface   = Surface,
            error     = ErrorRed,
            onPrimary = White,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
    }

    CompositionLocalProvider(LocalQuranColors provides quranColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = QuranTypography,
            content     = content,
        )
    }
}
