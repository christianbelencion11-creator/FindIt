package com.example.findit.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme: premium fresh green palette
val FindItBlue           = Color(0xFF16A34A)
val FindItBlueDeep       = Color(0xFF064E3B)
val FindItBlueBright     = Color(0xFF2DD4BF)
val FindItBlueLight      = Color(0xFFDDFCE8)
val FindItBlueUltraLight = Color(0xFFF0FDF4)
val FindItBackground     = Color(0xFFF7FBF6)
val FindItSurface        = Color(0xFFFFFFFF)
val FindItOnPrimary      = Color(0xFFFFFFFF)
val FindItOnSurface      = Color(0xFF102015)
val FindItOnSurfaceSoft  = Color(0xFF66756A)
val FindItError          = Color(0xFFDC2626)
val FindItGradientStart  = Color(0xFF064E3B)
val FindItGradientEnd    = Color(0xFF22C55E)

// Dark theme — "midnight emerald": near-black charcoal base with soft green glow.
// Only the FILL tokens are dark; text tokens stay light so contrast is preserved.
val FindItBlueDark          = Color(0xFF4ADE80)  // vivid emerald accent / outline on dark
val FindItBlueLightDark     = Color(0xFF213D2E)  // subtle green-tinted container (avatars/chips), lifted above surface
val FindItBackgroundDark    = Color(0xFF070C0A)  // near-black app background, faint cool-green tint
val FindItSurfaceDark       = Color(0xFF111A15)  // card surface — charcoal green, clearly above background
val FindItSurfaceVarDark    = Color(0xFF18231C)  // search field / chips — a touch lighter than surface
/** Dark card lift (top of subtle vertical gradient). */
val FindItSurfaceGradTopDark = Color(0xFF1A271F)
/** Dark card anchor (bottom of subtle vertical gradient). */
val FindItSurfaceGradBottomDark = Color(0xFF0C1310)
val FindItOnSurfaceDark     = Color(0xFFE7F2EA)
val FindItGradientStartDark = Color(0xFF0C3D28)  // header top-left — deep emerald
val FindItGradientEndDark   = Color(0xFF07140F)  // header bottom-right — melts into background

// Premium dashboard accents — vivid (not green-only)
val StatBlue       = Color(0xFF3B82F6)
val StatBlueDark   = Color(0xFF1D4ED8)
val StatPurple     = Color(0xFFA78BFA)
val StatPurpleDark = Color(0xFF7C3AED)
val StatGreen      = Color(0xFFFACC15)
val StatGreenDark  = Color(0xFFEAB308)
// "Your Overview" dashboard badges — solid accents shown at low alpha behind an outline icon.
val StatEmerald    = Color(0xFF34D399)  // Total Items — emerald that pops on the near-black panel
val StatAmber      = Color(0xFFFACC15)  // Recent — warm amber (same hue as legacy StatGreen)
