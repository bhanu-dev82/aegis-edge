package com.bhanu.aegis.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ChakuliShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),  // text fields, chips
    large      = RoundedCornerShape(24.dp),  // cards, primary buttons
    extraLarge = RoundedCornerShape(28.dp),  // bottom sheets, dialogs
)

// Asymmetric chat bubble shapes
// User's own bubbles — rounded everywhere except bottom-end corner ("speech tail")
val UserBubbleShape = RoundedCornerShape(
    topStart    = 18.dp,
    topEnd      = 18.dp,
    bottomStart = 18.dp,
    bottomEnd   = 4.dp,
)

// Model's response bubbles — rounded everywhere except bottom-start corner
val ModelBubbleShape = RoundedCornerShape(
    topStart    = 18.dp,
    topEnd      = 18.dp,
    bottomStart = 4.dp,
    bottomEnd   = 18.dp,
)

// Pill shape for FABs and send button
val PillShape = RoundedCornerShape(50)

// Thinking / tool card — slightly softer than card
val ThinkingBlockShape = RoundedCornerShape(12.dp)
val ToolCallCardShape  = RoundedCornerShape(12.dp)
