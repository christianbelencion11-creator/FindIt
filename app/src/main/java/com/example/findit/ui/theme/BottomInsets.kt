package com.example.findit.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Bottom padding for scrollable main-tab content so the last item clears the
 * floating bottom nav and the system gesture/navigation bar.
 */
@Composable
fun mainTabBottomScrollPadding(bottomNavVisible: Boolean): Dp {
    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navClearance = if (bottomNavVisible) {
        Dimensions.floatingBottomNavClearance
    } else {
        Spacing.lg
    }
    return systemBottom + navClearance
}
