package com.bhanu.aegis.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import com.bhanu.aegis.R

val NunitoFamily = FontFamily(
    Font(R.font.nunito_extralight, FontWeight.ExtraLight),
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

// Apply Nunito across the entire M3 type scale
private val baseline = Typography()

val ChakuliTypography = Typography(
    displayLarge  = baseline.displayLarge.copy(fontFamily  = NunitoFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = NunitoFamily),
    displaySmall  = baseline.displaySmall.copy(fontFamily  = NunitoFamily),
    headlineLarge  = baseline.headlineLarge.copy(fontFamily  = NunitoFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = NunitoFamily),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily  = NunitoFamily),
    titleLarge  = baseline.titleLarge.copy(fontFamily  = NunitoFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = NunitoFamily),
    titleSmall  = baseline.titleSmall.copy(fontFamily  = NunitoFamily),
    bodyLarge  = baseline.bodyLarge.copy(fontFamily  = NunitoFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = NunitoFamily),
    bodySmall  = baseline.bodySmall.copy(fontFamily  = NunitoFamily),
    labelLarge  = baseline.labelLarge.copy(fontFamily  = NunitoFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = NunitoFamily),
    labelSmall  = baseline.labelSmall.copy(fontFamily  = NunitoFamily),
)
