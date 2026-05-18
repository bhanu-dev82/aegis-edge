package com.bhanu.aegis.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Glass-morphism card with frosted glass effect.
 *
 * Background: surfaceContainerLow — a proper opaque surface tone from the M3 scheme.
 * This gives the card a distinct, slightly elevated look against the page background
 * (surface) without any semi-transparent overlay tricks that caused the dark rectangle
 * artifact when shadow() was also applied.
 *
 * Border: outlineVariant — a warm tan/charcoal that reads clearly on both light and
 * dark themes. The previous glassBorder token (white at 15%) was invisible on light
 * backgrounds, making the border look harsh and out of place.
 *
 * Elevation is expressed through border opacity only — no shadow() needed.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    elevation: GlassElevation = GlassElevation.Medium,
    content: @Composable BoxScope.() -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(
        alpha = when (elevation) {
            GlassElevation.Low    -> 0.4f
            GlassElevation.Medium -> 0.7f
            GlassElevation.High   -> 1.0f
        }
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            ),
        content = content,
    )
}

enum class GlassElevation {
    Low,
    Medium,
    High
}
