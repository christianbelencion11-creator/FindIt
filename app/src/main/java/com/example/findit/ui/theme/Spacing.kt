package com.example.findit.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val xxs  = 2.dp
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 12.dp
    val lg   = 16.dp
    val xl   = 20.dp
    val xxl  = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
}

object Dimensions {
    val cardCornerRadius    = 20.dp
    val headerCornerRadius  = 28.dp
    val headerContentHome   = 156.dp  // icon row + greeting + name + weather chip
    val headerContentStd    = 124.dp  // title + subtitle
    val headerContentWithMenu = 124.dp  // menu + title + subtitle (same row)
    val headerContentActionsOnly = 56.dp  // Back / Refresh row only (no title in header)
    val headerContentTall   = 176.dp  // taller header (legacy; prefer headerContentStd)
    val headerContentCompact = 0.dp  // fully collapsed — body is edge-to-edge below status bar
    val bottomBarElevation       = 0.dp
    val cardElevation            = 2.dp
    /** Floating pill bottom nav clearance above the system nav bar. */
    val floatingBottomNavClearance = 88.dp
}
