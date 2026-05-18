package com.bhanu.aegis.core.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════
//  CHAKULI LIGHT PALETTE — Warm Parchment + Coral
// ═══════════════════════════════════════════════════════════════════

// Primary — Warm Coral / Terracotta
val primaryLight = Color(0xFFD4634A)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDAD2)
val onPrimaryContainerLight = Color(0xFF3B0B00)

// Secondary — Warm Amber / Gold
val secondaryLight = Color(0xFF9C6B2F)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFDEAD)
val onSecondaryContainerLight = Color(0xFF341100)

// Tertiary — Warm Sage / Olive
val tertiaryLight = Color(0xFF5B7340)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFDFF0C5)
val onTertiaryContainerLight = Color(0xFF1A2E05)

// Error
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)

// Backgrounds — Warm Cream / Parchment (NOT harsh white)
val backgroundLight = Color(0xFFFFF8F4)
val onBackgroundLight = Color(0xFF231917)
val surfaceLight = Color(0xFFFFF8F4)
val onSurfaceLight = Color(0xFF231917)

// Surface variants — warm sand tones
val surfaceVariantLight = Color(0xFFF5DED6)
val onSurfaceVariantLight = Color(0xFF53433E)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF1EB)
val surfaceContainerLight = Color(0xFFFDEBE3)
val surfaceContainerHighLight = Color(0xFFF7E5DD)
val surfaceContainerHighestLight = Color(0xFFF1DFD7)

// Outline
val outlineLight = Color(0xFF85736D)
val outlineVariantLight = Color(0xFFD8C2BA)

// Inverse
val inverseSurfaceLight = Color(0xFF392E2B)
val inverseOnSurfaceLight = Color(0xFFFFEDE7)
val inversePrimaryLight = Color(0xFFFFB4A1)

// Surface dim / bright / scrim
val surfaceDimLight = Color(0xFFE8D6CE)
val surfaceBrightLight = Color(0xFFFFF8F4)
val scrimLight = Color(0xFF000000)

// ═══════════════════════════════════════════════════════════════════
//  CHAKULI DARK PALETTE — Deep Warm Charcoal
// ═══════════════════════════════════════════════════════════════════

// Primary — Desaturated Coral (soft, not harsh against dark)
val primaryDark = Color(0xFFFFB4A1)
val onPrimaryDark = Color(0xFF5F1500)
val primaryContainerDark = Color(0xFF832100)
val onPrimaryContainerDark = Color(0xFFFFDAD2)

// Secondary — Soft Amber Gold
val secondaryDark = Color(0xFFFFB95E)
val onSecondaryDark = Color(0xFF522300)
val secondaryContainerDark = Color(0xFF744B18)
val onSecondaryContainerDark = Color(0xFFFFDEAD)

// Tertiary — Muted Sage
val tertiaryDark = Color(0xFFC3D4AA)
val onTertiaryDark = Color(0xFF2E4416)
val tertiaryContainerDark = Color(0xFF445B2B)
val onTertiaryContainerDark = Color(0xFFDFF0C5)

// Error
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

// Backgrounds — Deep Warm Cocoa (NOT cold grey or pure black)
val backgroundDark = Color(0xFF1A1110)
val onBackgroundDark = Color(0xFFF0DED7)
val surfaceDark = Color(0xFF1A1110)
val onSurfaceDark = Color(0xFFF0DED7)

// Surface variants — warm charcoal tones
val surfaceVariantDark = Color(0xFF53433E)
val onSurfaceVariantDark = Color(0xFFD8C2BA)
val surfaceContainerLowestDark = Color(0xFF140C0A)
val surfaceContainerLowDark = Color(0xFF231917)
val surfaceContainerDark = Color(0xFF271D1B)
val surfaceContainerHighDark = Color(0xFF322825)
val surfaceContainerHighestDark = Color(0xFF3D3330)

// Outline
val outlineDark = Color(0xFFA08D86)
val outlineVariantDark = Color(0xFF53433E)

// Inverse
val inverseSurfaceDark = Color(0xFFF0DED7)
val inverseOnSurfaceDark = Color(0xFF392E2B)
val inversePrimaryDark = Color(0xFFD4634A)

// Surface dim / bright / scrim
val surfaceDimDark = Color(0xFF1A1110)
val surfaceBrightDark = Color(0xFF423735)
val scrimDark = Color(0xFF000000)

// ═══════════════════════════════════════════════════════════════════
//  CUSTOM COLORS — Chat bubbles, thinking highlight, success, links
//  These are NOT part of M3 scheme — used via ChakuliCustomColors
// ═══════════════════════════════════════════════════════════════════

// User message bubble
val userBubbleBgLight = Color(0xFFFFDAD2)       // peach — warm + readable
val userBubbleBgDark = Color(0xFF5F2010)
val userBubbleTextLight = Color(0xFF3B0B00)
val userBubbleTextDark = Color(0xFFFFDAD2)

// Model message bubble
val modelBubbleBgLight = Color(0xFFEDE0D8)      // slightly darker warm sand for contrast
val modelBubbleBgDark = Color(0xFF3D2E2A)        // noticeably lighter than surface for contrast
val modelBubbleTextLight = Color(0xFF231917)
val modelBubbleTextDark = Color(0xFFF0DED7)

// Thinking / reasoning block
val thinkingBgLight = Color(0xFFFFF3CD)          // soft warm yellow
val thinkingBgDark = Color(0xFF3D3520)
val thinkingBorderLight = Color(0xFFE8C866)
val thinkingBorderDark = Color(0xFF7D6A2A)
val thinkingTextLight = Color(0xFF5C4A0A)
val thinkingTextDark = Color(0xFFE8C866)

// Tool call card
val toolCallBgLight = Color(0xFFEAF4E0)          // soft sage
val toolCallBgDark = Color(0xFF233018)
val toolCallBorderLight = Color(0xFF9DC879)
val toolCallBorderDark = Color(0xFF5B7340)

// Success
val successLight = Color(0xFF386A1F)
val successDark = Color(0xFF9DC879)

// Hyperlinks
val linkLight = Color(0xFF9C6B2F)                // warm amber for links
val linkDark = Color(0xFFFFB95E)

// ═══════════════════════════════════════════════════════════════════
//  GLASSMORPHISM & AURA TOKENS — frosted glass effects + Aura glow
// ═══════════════════════════════════════════════════════════════════

// Glass overlay for cards — frosted glass effect
val glassOverlayLight = Color(0x14FFFFFF)   // white 8%
val glassOverlayDark  = Color(0x1AFFFFFF)   // white 10%
val glassBorderLight  = Color(0x26FFFFFF)   // white 15%
val glassBorderDark   = Color(0x33FFFFFF)   // white 20%

// Aura animation gradient stops (for engine loading animation)
val auraCoral = Color(0xFFEE675C)
val auraGold  = Color(0xFFFFB95E)            // matches secondaryDark
val auraSage  = Color(0xFFC3D4AA)            // matches tertiaryDark

// ═══════════════════════════════════════════════════════════════════
//  AI GLOW ACCENTS — Subtle cool accents for premium AI feel
// ═══════════════════════════════════════════════════════════════════

// Subtle blue glow for AI elements (used sparingly)
val glowBlue = Color(0xFF7AA2FF)
val glowBlueDim = Color(0x407AA2FF)          // 25% opacity

// White glow for logo center and focus states
val glowWhite = Color(0xFFFFFFFF)
val glowWhiteDim = Color(0x66FFFFFF)         // 40% opacity

// ═══════════════════════════════════════════════════════════════════
//  TRIAGE COLORS — Aegis-Edge disaster triage patient queue
// ═══════════════════════════════════════════════════════════════════

// Triage priority colors (START Protocol)
val triageRed     = Color(0xFFE53935)  // Immediate
val triageYellow  = Color(0xFFFDD835)  // Delayed
val triageGreen   = Color(0xFF43A047)  // Minor
val triageBlack   = Color(0xFF212121)  // Deceased

// Triage card backgrounds (subtle, for light/dark)
val triageRedBgLight    = Color(0xFFFFEBEE)
val triageRedBgDark     = Color(0xFF3E1010)
val triageYellowBgLight = Color(0xFFFFFDE7)
val triageYellowBgDark  = Color(0xFF3D3510)
val triageGreenBgLight  = Color(0xFFE8F5E9)
val triageGreenBgDark   = Color(0xFF1B3518)
val triageBlackBgLight  = Color(0xFFF5F5F5)
val triageBlackBgDark   = Color(0xFF1A1A1A)

// Emergency accent gradient (hero card)
val emergencyGradientStart = Color(0xFFE53935)
val emergencyGradientEnd   = Color(0xFFFF6F00)

// Offline indicator
val offlineGreen = Color(0xFF00C853)


