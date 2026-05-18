package com.bhanu.aegis.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bhanu.aegis.R

/**
 * Chakuli flower logo - 5 petals with optional glowing animation
 * Uses the actual logo SVG converted to vector drawable
 */
@Composable
fun ChakuliLogo(
    size: Dp = 48.dp,
    variant: LogoVariant = LogoVariant.Static,
    modifier: Modifier = Modifier,
) {
    // Only animate if not static
    val shouldAnimate = variant != LogoVariant.Static
    
    val glowAlpha = if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (variant) {
                        LogoVariant.Pulsing -> 2500
                        LogoVariant.Thinking -> 1200
                        else -> 2500
                    },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        ).value
    } else {
        0f // No glow for static
    }
    
    val glowRadius = if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "logo_glow_radius")
        infiniteTransition.animateFloat(
            initialValue = size.value * 0.4f,
            targetValue = size.value * 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = when (variant) {
                        LogoVariant.Pulsing -> 2500
                        LogoVariant.Thinking -> 1200
                        else -> 2500
                    },
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_radius"
        ).value
    } else {
        0f
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect behind the logo (only for animated variants)
        if (shouldAnimate && glowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(size)
                    .drawBehind {
                        drawGlow(
                            color = Color.White,
                            alpha = glowAlpha,
                            radius = glowRadius
                        )
                    }
            )
        }
        
        // The actual logo
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_chakuli_logo),
            contentDescription = "Chakuli",
            modifier = Modifier.size(size),
            tint = Color.White
        )
    }
}

private fun DrawScope.drawGlow(
    color: Color,
    alpha: Float,
    radius: Float
) {
    val center = this.center
    
    // Draw multiple circles with decreasing alpha for glow effect
    for (i in 3 downTo 1) {
        drawCircle(
            color = color.copy(alpha = alpha / (4 - i)),
            radius = radius * (i / 3f),
            center = center
        )
    }
}

enum class LogoVariant {
    Static,    // No animation
    Pulsing,   // Slow gentle pulse (2.5s)
    Thinking   // Faster pulse for loading (1.2s)
}
