package com.example.findit.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.findit.navigation.BottomNavTab
import com.example.findit.ui.components.AddSpeedDialMenu
import com.example.findit.ui.components.FindItBottomBar
import com.example.findit.ui.components.FindItNavigationDrawerContent
import com.example.findit.viewmodel.ItemViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val NAV_IDLE_HIDE_MS = 4_000L

@Composable
fun MainScreen(
    viewModel: ItemViewModel,
    onItemClick: (Long) -> Unit,
    onEditItem: (Long) -> Unit = {},
    themeMode: com.example.findit.ui.theme.ThemeMode = com.example.findit.ui.theme.ThemeMode.Auto,
    onThemeModeChanged: (com.example.findit.ui.theme.ThemeMode) -> Unit = {},
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    showProfilePhotoUpdated: Boolean = false,
    onProfilePhotoUpdatedDismissed: () -> Unit = {},
    displayName: String = "IRemember User",
    username: String = "iremember_user",
    bio: String = "Keeping everyday essentials organized and easy to find.",
    fullName: String = "",
    birthday: String = "",
    family: String = "",
    phone: String = "",
    location: String = "",
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onNotesClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAllItemsClick: () -> Unit = {},
    onAllCategoriesClick: () -> Unit = {},
    onRecentItemsClick: () -> Unit = {},
    openSearchSignal: StateFlow<Boolean> = MutableStateFlow(false),
    onOpenSearchConsumed: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.Home) }
    var bottomNavVisible by rememberSaveable { mutableStateOf(true) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var launchVoiceSearch by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openSearch by openSearchSignal.collectAsState()

    LaunchedEffect(openSearch) {
        if (openSearch) {
            selectedTab = BottomNavTab.Search
            onOpenSearchConsumed()
        }
    }

    LaunchedEffect(showProfilePhotoUpdated) {
        if (showProfilePhotoUpdated) {
            selectedTab = BottomNavTab.Profile
        }
    }

    fun onUserInteraction() {
        lastInteraction = System.currentTimeMillis()
        bottomNavVisible = true
    }

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    LaunchedEffect(selectedTab, lastInteraction, fabMenuExpanded) {
        bottomNavVisible = true
        if (fabMenuExpanded) return@LaunchedEffect
        delay(NAV_IDLE_HIDE_MS)
        bottomNavVisible = false
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen && selectedTab != BottomNavTab.Home) {
        selectedTab = BottomNavTab.Home
        onUserInteraction()
    }

    // Highest priority when the add-menu is open: back just closes it.
    BackHandler(enabled = fabMenuExpanded) {
        fabMenuExpanded = false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent,
                drawerContentColor = Color.Unspecified
            ) {
                FindItNavigationDrawerContent(
                    displayName = displayName,
                    username = username,
                    profileImageUri = profileImageUri,
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged,
                    onHome = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.Home }
                    },
                    onSearch = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.Search }
                    },
                    onAddItem = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.AddItem }
                    },
                    onAlerts = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.Alerts }
                    },
                    onNews = {
                        onUserInteraction()
                        closeDrawerThen { onNotificationsClick() }
                    },
                    onHistory = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.History }
                    },
                    onNotes = {
                        onUserInteraction()
                        closeDrawerThen { onNotesClick() }
                    },
                    onProfile = {
                        onUserInteraction()
                        closeDrawerThen { selectedTab = BottomNavTab.Profile }
                    },
                    onSignOut = {
                        closeDrawerThen(onLogout)
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { onUserInteraction() }
                    },
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) {
                        if (targetState.ordinal > initialState.ordinal) it else -it
                    })
                        .togetherWith(
                            fadeOut(tween(300)) + slideOutHorizontally(tween(300)) {
                                if (targetState.ordinal > initialState.ordinal) -it else it
                            }
                        )
                },
                label = "bottom_nav_content"
            ) { tab ->
                when (tab) {
                    BottomNavTab.Home -> HomeScreen(
                        viewModel = viewModel,
                        onSearchClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Search
                        },
                        onItemClick = onItemClick,
                        embedded = true,
                        onAddItemClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.AddItem
                        },
                        onEditItem = onEditItem,
                        onProfileClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Profile
                        },
                        onMenuClick = {
                            onUserInteraction()
                            scope.launch { drawerState.open() }
                        },
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        profileImageUri = profileImageUri,
                        displayName = displayName,
                        username = username,
                        onNotificationsClick = {
                            onUserInteraction()
                            onNotificationsClick()
                        },
                        onAlertsClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Alerts
                        },
                        onLocationClick = { loc ->
                            onUserInteraction()
                            viewModel.updateSearchQuery(loc)
                            selectedTab = BottomNavTab.Search
                        },
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible,
                        onTotalItemsClick = {
                            onUserInteraction()
                            onAllItemsClick()
                        },
                        onCategoriesClick = {
                            onUserInteraction()
                            onAllCategoriesClick()
                        },
                        onRecentClick = {
                            onUserInteraction()
                            onRecentItemsClick()
                        },
                        onVoiceSearchClick = {
                            onUserInteraction()
                            launchVoiceSearch = true
                            selectedTab = BottomNavTab.Search
                        },
                        onNotesClick = {
                            onUserInteraction()
                            onNotesClick()
                        }
                    )
                    BottomNavTab.Search -> SearchScreen(
                        viewModel = viewModel,
                        onItemClick = onItemClick,
                        embedded = true,
                        onBackClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Home
                        },
                        onAddItemClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.AddItem
                        },
                        onMenuClick = {
                            onUserInteraction()
                            scope.launch { drawerState.open() }
                        },
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible,
                        launchVoiceSearch = launchVoiceSearch,
                        onVoiceSearchLaunched = { launchVoiceSearch = false },
                        onNotesClick = {
                            onUserInteraction()
                            onNotesClick()
                        }
                    )
                    BottomNavTab.AddItem -> AddItemScreen(
                        viewModel = viewModel,
                        embedded = true,
                        onSaveSuccess = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Home
                        },
                        onBackClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Home
                        },
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible
                    )
                    BottomNavTab.Alerts -> AlertsScreen(
                        viewModel = viewModel,
                        onItemClick = onItemClick,
                        onMenuClick = {
                            onUserInteraction()
                            scope.launch { drawerState.open() }
                        },
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible
                    )
                    BottomNavTab.History -> HistoryScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            onUserInteraction()
                            selectedTab = BottomNavTab.Home
                        },
                        onItemClick = onItemClick,
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible
                    )
                    BottomNavTab.Profile -> ProfileScreen(
                        viewModel = viewModel,
                        themeMode = themeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        profileImageUri = profileImageUri,
                        onProfileImageChanged = onProfileImageChanged,
                        showProfilePhotoUpdated = showProfilePhotoUpdated,
                        onProfilePhotoUpdatedDismissed = onProfilePhotoUpdatedDismissed,
                        displayName = displayName,
                        username = username,
                        bio = bio,
                        fullName = fullName,
                        birthday = birthday,
                        family = family,
                        phone = phone,
                        location = location,
                        onEditProfileClick = {
                            onUserInteraction()
                            onEditProfile()
                        },
                        onChangePassword = {
                            onUserInteraction()
                            onChangePassword()
                        },
                        onAboutClick = {
                            onUserInteraction()
                            onAboutClick()
                        },
                        onLogout = onLogout,
                        onMenuClick = {
                            onUserInteraction()
                            scope.launch { drawerState.open() }
                        },
                        onUserInteraction = ::onUserInteraction,
                        bottomNavVisible = bottomNavVisible,
                        onTotalItemsClick = {
                            onUserInteraction()
                            onAllItemsClick()
                        },
                        onCategoriesClick = {
                            onUserInteraction()
                            onAllCategoriesClick()
                        },
                        onRecentClick = {
                            onUserInteraction()
                            onRecentItemsClick()
                        }
                    )
                }
            }

            AddSpeedDialMenu(
                expanded = fabMenuExpanded,
                onDismiss = { fabMenuExpanded = false },
                onAddItem = {
                    fabMenuExpanded = false
                    onUserInteraction()
                    selectedTab = BottomNavTab.AddItem
                },
                onNotes = {
                    fabMenuExpanded = false
                    onUserInteraction()
                    onNotesClick()
                },
                onMicrophone = {
                    fabMenuExpanded = false
                    onUserInteraction()
                    launchVoiceSearch = true
                    selectedTab = BottomNavTab.Search
                }
            )

            AnimatedVisibility(
                visible = bottomNavVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(animationSpec = tween(250)) { it } + fadeIn(tween(250)),
                exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(250))
            ) {
                FindItBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        onUserInteraction()
                        fabMenuExpanded = false
                        selectedTab = tab
                    },
                    onAddClick = {
                        onUserInteraction()
                        fabMenuExpanded = !fabMenuExpanded
                    },
                    addExpanded = fabMenuExpanded
                )
            }
        }
    }
}
