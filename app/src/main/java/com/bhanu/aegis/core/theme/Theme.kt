package com.bhanu.aegis.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────
//  M3 Color Schemes
// ─────────────────────────────────────────────

private val ChakuliLightColorScheme = lightColorScheme(
    primary              = primaryLight,
    onPrimary            = onPrimaryLight,
    primaryContainer     = primaryContainerLight,
    onPrimaryContainer   = onPrimaryContainerLight,
    secondary            = secondaryLight,
    onSecondary          = onSecondaryLight,
    secondaryContainer   = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary             = tertiaryLight,
    onTertiary           = onTertiaryLight,
    tertiaryContainer    = tertiaryContainerLight,
    onTertiaryContainer  = onTertiaryContainerLight,
    error                = errorLight,
    onError              = onErrorLight,
    errorContainer       = errorContainerLight,
    onErrorContainer     = onErrorContainerLight,
    background           = backgroundLight,
    onBackground         = onBackgroundLight,
    surface              = surfaceLight,
    onSurface            = onSurfaceLight,
    surfaceVariant       = surfaceVariantLight,
    onSurfaceVariant     = onSurfaceVariantLight,
    outline              = outlineLight,
    outlineVariant       = outlineVariantLight,
    scrim                = scrimLight,
    inverseSurface       = inverseSurfaceLight,
    inverseOnSurface     = inverseOnSurfaceLight,
    inversePrimary       = inversePrimaryLight,
    surfaceDim           = surfaceDimLight,
    surfaceBright        = surfaceBrightLight,
    surfaceContainerLowest  = surfaceContainerLowestLight,
    surfaceContainerLow     = surfaceContainerLowLight,
    surfaceContainer        = surfaceContainerLight,
    surfaceContainerHigh    = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val ChakuliDarkColorScheme = darkColorScheme(
    primary              = primaryDark,
    onPrimary            = onPrimaryDark,
    primaryContainer     = primaryContainerDark,
    onPrimaryContainer   = onPrimaryContainerDark,
    secondary            = secondaryDark,
    onSecondary          = onSecondaryDark,
    secondaryContainer   = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary             = tertiaryDark,
    onTertiary           = onTertiaryDark,
    tertiaryContainer    = tertiaryContainerDark,
    onTertiaryContainer  = onTertiaryContainerDark,
    error                = errorDark,
    onError              = onErrorDark,
    errorContainer       = errorContainerDark,
    onErrorContainer     = onErrorContainerDark,
    background           = backgroundDark,
    onBackground         = onBackgroundDark,
    surface              = surfaceDark,
    onSurface            = onSurfaceDark,
    surfaceVariant       = surfaceVariantDark,
    onSurfaceVariant     = onSurfaceVariantDark,
    outline              = outlineDark,
    outlineVariant       = outlineVariantDark,
    scrim                = scrimDark,
    inverseSurface       = inverseSurfaceDark,
    inverseOnSurface     = inverseOnSurfaceDark,
    inversePrimary       = inversePrimaryDark,
    surfaceDim           = surfaceDimDark,
    surfaceBright        = surfaceBrightDark,
    surfaceContainerLowest  = surfaceContainerLowestDark,
    surfaceContainerLow     = surfaceContainerLowDark,
    surfaceContainer        = surfaceContainerDark,
    surfaceContainerHigh    = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

// ─────────────────────────────────────────────
//  Custom Colors (beyond M3 scheme)
// ─────────────────────────────────────────────

@Immutable
data class ChakuliCustomColors(
    // Chat bubbles
    val userBubbleBg: Color,
    val userBubbleText: Color,
    val modelBubbleBg: Color,
    val modelBubbleText: Color,
    // Thinking blocks
    val thinkingBg: Color,
    val thinkingBorder: Color,
    val thinkingText: Color,
    // Tool call cards
    val toolCallBg: Color,
    val toolCallBorder: Color,
    // Semantic
    val success: Color,
    val link: Color,
    // Glassmorphism
    val glassOverlay: Color,
    val glassBorder: Color,
)

val LocalChakuliColors = staticCompositionLocalOf {
    ChakuliCustomColors(
        userBubbleBg     = userBubbleBgLight,
        userBubbleText   = userBubbleTextLight,
        modelBubbleBg    = modelBubbleBgLight,
        modelBubbleText  = modelBubbleTextLight,
        thinkingBg       = thinkingBgLight,
        thinkingBorder   = thinkingBorderLight,
        thinkingText     = thinkingTextLight,
        toolCallBg       = toolCallBgLight,
        toolCallBorder   = toolCallBorderLight,
        success          = successLight,
        link             = linkLight,
        glassOverlay     = glassOverlayLight,
        glassBorder      = glassBorderLight,
    )
}

// Convenience accessor — use inside @Composable as `ChakuliTheme.colors.userBubbleBg`
object ChakuliTheme {
    val colors: ChakuliCustomColors
        @Composable get() = LocalChakuliColors.current
}

// ─────────────────────────────────────────────
//  ChakuliTheme Composable
// ─────────────────────────────────────────────

@Composable
fun ChakuliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChakuliDarkColorScheme else ChakuliLightColorScheme

    val customColors = if (darkTheme) {
        ChakuliCustomColors(
            userBubbleBg    = userBubbleBgDark,
            userBubbleText  = userBubbleTextDark,
            modelBubbleBg   = modelBubbleBgDark,
            modelBubbleText = modelBubbleTextDark,
            thinkingBg      = thinkingBgDark,
            thinkingBorder  = thinkingBorderDark,
            thinkingText    = thinkingTextDark,
            toolCallBg      = toolCallBgDark,
            toolCallBorder  = toolCallBorderDark,
            success         = successDark,
            link            = linkDark,
            glassOverlay    = glassOverlayDark,
            glassBorder     = glassBorderDark,
        )
    } else {
        ChakuliCustomColors(
            userBubbleBg    = userBubbleBgLight,
            userBubbleText  = userBubbleTextLight,
            modelBubbleBg   = modelBubbleBgLight,
            modelBubbleText = modelBubbleTextLight,
            thinkingBg      = thinkingBgLight,
            thinkingBorder  = thinkingBorderLight,
            thinkingText    = thinkingTextLight,
            toolCallBg      = toolCallBgLight,
            toolCallBorder  = toolCallBorderLight,
            success         = successLight,
            link            = linkLight,
            glassOverlay    = glassOverlayLight,
            glassBorder     = glassBorderLight,
        )
    }

    // Make status bar transparent and adapt icon color to theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalChakuliColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = ChakuliTypography,
            shapes      = ChakuliShapes,
            content     = content,
        )
    }
}
