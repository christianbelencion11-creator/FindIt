package com.example.findit.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.findit.navigation.BottomNavTab
import com.example.findit.ui.components.FindItBottomBar
import com.example.findit.ui.theme.gradientStartColor
import com.example.findit.viewmodel.ItemViewModel

@Composable
fun MainScreen(
    viewModel: ItemViewModel,
    onItemClick: (Long) -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    displayName: String = "FindIt User",
    username: String = "findit_user",
    bio: String = "Keeping everyday essentials organized and easy to find.",
    onProfileDetailsChanged: (String, String, String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = gradientStartColor(),
        bottomBar = {
            FindItBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            transitionSpec = {
                (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { if (targetState.ordinal > initialState.ordinal) it else -it })
                    .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { if (targetState.ordinal > initialState.ordinal) -it else it })
            },
            label = "bottom_nav_content"
        ) { tab ->
            when (tab) {
                BottomNavTab.Home -> HomeScreen(
                    viewModel = viewModel,
                    onSearchClick = { selectedTab = BottomNavTab.Search },
                    onItemClick = onItemClick,
                    embedded = true,
                    onAddItemClick = { selectedTab = BottomNavTab.AddItem },
                    onProfileClick = { selectedTab = BottomNavTab.Profile },
                    profileImageUri = profileImageUri,
                    displayName = displayName,
                    username = username,
                    onNotificationsClick = {},
                    onSettingsClick = { selectedTab = BottomNavTab.Profile },
                    onLocationClick = { loc ->
                        viewModel.updateSearchQuery(loc)
                        selectedTab = BottomNavTab.Search
                    }
                )
                BottomNavTab.Search -> SearchScreen(
                    viewModel = viewModel,
                    onItemClick = onItemClick,
                    embedded = true
                )
                BottomNavTab.AddItem -> AddItemScreen(
                    viewModel = viewModel,
                    embedded = true,
                    onSaveSuccess = { selectedTab = BottomNavTab.Home }
                )
                BottomNavTab.Profile -> ProfileScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChanged = onDarkThemeChanged,
                    profileImageUri = profileImageUri,
                    onProfileImageChanged = onProfileImageChanged,
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    onProfileDetailsChanged = onProfileDetailsChanged,
                    onLogout = onLogout
                )
            }
        }
    }
}
