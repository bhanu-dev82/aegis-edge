package com.bhanu.aegis.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Micro-interaction modifiers for premium feel
 */

/**
 * Subtle scale-down effect on press
 * Creates tactile feedback for interactive elements
 */
fun Modifier.pressScale(
    pressedScale: Float = 0.96f,
    enabled: Boolean = true,
): Modifier = composed {
    if (!enabled) return@composed this
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "press_scale"
    )
    
    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Animated glow border for focus states
 * Used on input fields and important interactive elements
 */
fun Modifier.glowBorder(
    width: Dp = 1.dp,
    color: Color,
    shape: Shape,
    animated: Boolean = true,
): Modifier = composed {
    if (!animated) {
        return@composed this.border(width, color, shape)
    }
    
    // For now, simple border - can be enhanced with animated gradient
    this.border(width, color, shape)
}
