package com.eltonkola.everything.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import everything.composeapp.generated.resources.Res
import everything.composeapp.generated.resources.opensans_italic_variablefont_wdth_wght
import everything.composeapp.generated.resources.opensans_variablefont_wdth_wght
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OpenSans() = FontFamily(
    Font(Res.font.opensans_variablefont_wdth_wght, weight = FontWeight.Normal),
    Font(Res.font.opensans_variablefont_wdth_wght, weight = FontWeight.Medium),
    Font(Res.font.opensans_variablefont_wdth_wght, weight = FontWeight.Bold),
    Font(Res.font.opensans_italic_variablefont_wdth_wght, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(Res.font.opensans_italic_variablefont_wdth_wght, weight = FontWeight.Bold, style = FontStyle.Italic)
)

@Composable
fun TekoTypography() = Typography().run {

    val fontFamily = OpenSans()
    copy(

        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
    )
}

