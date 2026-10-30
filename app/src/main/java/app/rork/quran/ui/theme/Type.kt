package app.rork.quran.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.rork.quran.R

val KfgqpcFont = FontFamily(Font(R.font.kfgqpc))

@OptIn(ExperimentalTextApi::class)
val CairoFont = FontFamily(
    *listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(R.font.cairo, weight = weight,
            variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
    }.toTypedArray(),
)

private val BaseTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CairoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
    ),
)

// Apply Cairo to every Material text style; verse components keep their explicit Quran font.
val QuranTypography = Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = CairoFont),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = CairoFont),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = CairoFont),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = CairoFont),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = CairoFont),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = CairoFont),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = CairoFont),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = CairoFont),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = CairoFont),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = CairoFont),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = CairoFont),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = CairoFont),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = CairoFont),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = CairoFont),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = CairoFont),
)
