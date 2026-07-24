package com.example.findit.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.findit.FindItApplication
import com.example.findit.auth.FirebaseAuthRepository
import com.example.findit.auth.SecretRecoveryRepository
import com.example.findit.auth.UsernameAuth
import com.example.findit.screens.ItemDetailScreen
import com.example.findit.screens.AboutScreen
import com.example.findit.screens.AddItemScreen
import com.example.findit.screens.HistoryScreen
import com.example.findit.screens.MainScreen
import com.example.findit.screens.NewsScreen
import com.example.findit.screens.NoteEditorScreen
import com.example.findit.screens.NotesScreen
import com.example.findit.screens.PrivacyPolicyScreen
import com.example.findit.screens.ProfilePhotoCropScreen
import com.example.findit.screens.EditProfileScreen
import com.example.findit.screens.SplashScreen
import com.example.findit.screens.StatsBrowseMode
import com.example.findit.screens.StatsBrowseScreen
import com.example.findit.screens.auth.ChangePasswordScreen
import com.example.findit.screens.auth.ForgotPasswordScreen
import com.example.findit.screens.auth.OnboardingScreen
import com.example.findit.screens.auth.LoginScreen
import com.example.findit.screens.auth.RegisterScreen
import com.example.findit.screens.auth.SetupPinScreen
import com.example.findit.screens.auth.UnlockScreen
import com.example.findit.util.AuthPreferences
import com.example.findit.util.ProfilePreferences
import com.example.findit.util.ProfileStore
import com.example.findit.ui.theme.ThemeMode
import com.example.findit.viewmodel.ItemViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun FindItNavHost(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    themeMode: ThemeMode = ThemeMode.Auto,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    profileStore: ProfileStore,
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    displayName: String = ProfilePreferences.DEFAULT_DISPLAY_NAME,
    username: String = ProfilePreferences.DEFAULT_USERNAME,
    bio: String = ProfilePreferences.DEFAULT_BIO,
    fullName: String = "",
    birthday: String = "",
    family: String = "",
    phone: String = "",
    location: String = "",
    onProfileDetailsChanged: (com.example.findit.screens.ProfileDetailsUpdate) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as FindItApplication
    val authPreferences = remember { AuthPreferences(context) }
    val authRepository = remember {
        FirebaseAuthRepository(
            authPreferences,
            application.repository,
            application.noteRepository
        )
    }
    val recoveryRepository = remember { SecretRecoveryRepository() }
    val viewModel: ItemViewModel = viewModel(
        factory = ItemViewModel.Factory
    )

    fun syncProfileForUser(user: FirebaseAuthRepository.SignedInUser) {
        val resolvedUsername = UsernameAuth.usernameFromSyntheticEmail(user.email)
            ?: user.displayName.takeIf { it.isNotBlank() }
            ?: ProfilePreferences.DEFAULT_USERNAME
        profileStore.syncFromAuthUser(
            firebaseUid = user.uid,
            displayName = user.displayName.ifBlank { resolvedUsername },
            username = resolvedUsername,
            photoUrl = user.photoUrl
        )
    }

    fun restoreItemOwnerFromSession() {
        val uid = authRepository.currentUser?.uid ?: authPreferences.getFirebaseUid()
        if (uid.isNotBlank()) {
            application.repository.setOwnerUid(uid)
            application.noteRepository.setOwnerUid(uid)
        }
    }

    fun navigateToMainAfterAuth(user: FirebaseAuthRepository.SignedInUser) {
        authPreferences.setHasSeenGetStarted(true)
        application.repository.setOwnerUid(user.uid)
        application.noteRepository.setOwnerUid(user.uid)
        syncProfileForUser(user)
        if (authPreferences.isPinSet(user.uid)) {
            navController.navigate(Routes.UNLOCK) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.SETUP_PIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun leaveGetStarted(destination: String) {
        authPreferences.setHasSeenGetStarted(true)
        navController.navigate(destination) {
            popUpTo(Routes.GET_STARTED) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                authRepository = authRepository,
                authPreferences = authPreferences,
                onRestoreSession = { restoreItemOwnerFromSession() },
                onNavigateToGetStarted = {
                    navController.navigate(Routes.GET_STARTED) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToUnlock = {
                    navController.navigate(Routes.UNLOCK) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToSetupPin = {
                    navController.navigate(Routes.SETUP_PIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.GET_STARTED) {
            OnboardingScreen(
                onCreateAccount = { leaveGetStarted(Routes.REGISTER) },
                onAlreadyHaveAccount = { leaveGetStarted(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = authRepository,
                authPreferences = authPreferences,
                onLoginSuccess = { user -> navigateToMainAfterAuth(user) },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = { username ->
                    navController.navigate(Routes.forgotPassword(username))
                }
            )
        }

        composable(
            route = Routes.FORGOT_PASSWORD,
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val rawUsername = backStackEntry.arguments?.getString("username").orEmpty()
            val initialUsername = try {
                URLDecoder.decode(rawUsername, StandardCharsets.UTF_8.name())
            } catch (_: Exception) {
                rawUsername
            }
            ForgotPasswordScreen(
                recoveryRepository = recoveryRepository,
                initialUsername = initialUsername,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authRepository = authRepository,
                authPreferences = authPreferences,
                onRegisterSuccess = { user -> navigateToMainAfterAuth(user) },
                onNavigateToLogin = {
                    // Must navigate explicitly: after Get Started → Register, Login is not under this
                    // destination, so popBackStack() alone does nothing.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onPrivacyPolicyClick = {
                    navController.navigate(Routes.PRIVACY_POLICY)
                }
            )
        }

        composable(Routes.SETUP_PIN) {
            SetupPinScreen(
                authPreferences = authPreferences,
                onComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SETUP_PIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.UNLOCK) {
            UnlockScreen(
                authPreferences = authPreferences,
                onUnlocked = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.UNLOCK) { inclusive = true }
                    }
                },
                onUsePassword = {
                    authRepository.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.UNLOCK) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val mainEntry = navController.getBackStackEntry(Routes.MAIN)
            var showProfilePhotoUpdated by remember { mutableStateOf(false) }
            val photoUpdatedFlag = mainEntry.savedStateHandle
                .getStateFlow("profile_photo_updated", false)
            val photoUpdated by photoUpdatedFlag.collectAsState(initial = false)
            LaunchedEffect(photoUpdated) {
                if (photoUpdated) {
                    showProfilePhotoUpdated = true
                    mainEntry.savedStateHandle["profile_photo_updated"] = false
                }
            }

            MainScreen(
                viewModel = viewModel,
                onItemClick = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                },
                onEditItem = { itemId ->
                    navController.navigate(Routes.editItem(itemId))
                },
                themeMode = themeMode,
                onThemeModeChanged = onThemeModeChanged,
                profileImageUri = profileImageUri,
                onProfileImageChanged = { uri ->
                    navController.navigate(Routes.profileCrop(uri))
                },
                showProfilePhotoUpdated = showProfilePhotoUpdated,
                onProfilePhotoUpdatedDismissed = { showProfilePhotoUpdated = false },
                displayName = displayName,
                username = username,
                bio = bio,
                fullName = fullName,
                birthday = birthday,
                family = family,
                phone = phone,
                location = location,
                onEditProfile = {
                    navController.navigate(Routes.EDIT_PROFILE)
                },
                onChangePassword = {
                    navController.navigate(Routes.CHANGE_PASSWORD)
                },
                onNotificationsClick = {
                    navController.navigate(Routes.NEWS)
                },
                onHistoryClick = {
                    navController.navigate(Routes.HISTORY)
                },
                onNotesClick = {
                    navController.navigate(Routes.NOTES)
                },
                onAboutClick = {
                    navController.navigate(Routes.ABOUT)
                },
                onAllItemsClick = {
                    navController.navigate(Routes.ALL_ITEMS)
                },
                onAllCategoriesClick = {
                    navController.navigate(Routes.ALL_CATEGORIES)
                },
                onRecentItemsClick = {
                    navController.navigate(Routes.RECENT_ITEMS)
                },
                openSearchSignal = mainEntry.savedStateHandle
                    .getStateFlow("open_search", false),
                onOpenSearchConsumed = {
                    mainEntry.savedStateHandle["open_search"] = false
                },
                onLogout = {
                    authRepository.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ALL_ITEMS) {
            StatsBrowseScreen(
                mode = StatsBrowseMode.AllItems,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) }
            )
        }

        composable(Routes.ALL_CATEGORIES) {
            StatsBrowseScreen(
                mode = StatsBrowseMode.Categories,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) },
                onCategoryOpenSearch = { cat ->
                    viewModel.updateSearchQuery(cat)
                    runCatching {
                        navController.getBackStackEntry(Routes.MAIN)
                            .savedStateHandle["open_search"] = true
                    }
                    navController.popBackStack(Routes.MAIN, inclusive = false)
                }
            )
        }

        composable(Routes.RECENT_ITEMS) {
            StatsBrowseScreen(
                mode = StatsBrowseMode.Recent,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                profileImageUri = profileImageUri,
                onProfileImageChanged = { uri ->
                    navController.navigate(Routes.profileCrop(uri))
                },
                displayName = displayName,
                username = username,
                bio = bio,
                fullName = fullName,
                birthday = birthday,
                family = family,
                phone = phone,
                location = location,
                onSave = onProfileDetailsChanged,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                authRepository = authRepository,
                username = username,
                onCancel = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.NEWS) {
            NewsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                }
            )
        }

        composable(Routes.NOTES) {
            NotesScreen(
                onBackClick = { navController.popBackStack() },
                onOpenNote = { noteId ->
                    navController.navigate(Routes.noteEditor(noteId))
                },
                onCreateNote = {
                    navController.navigate(Routes.noteEditor(0L))
                }
            )
        }

        composable(
            route = Routes.NOTE_EDITOR,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            NoteEditorScreen(
                noteId = noteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyPolicyClick = {
                    navController.navigate(Routes.PRIVACY_POLICY)
                }
            )
        }

        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PROFILE_CROP,
            arguments = listOf(
                navArgument("uri") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val rawUri = backStackEntry.arguments?.getString("uri").orEmpty()
            val sourceUri = try {
                URLDecoder.decode(rawUri, StandardCharsets.UTF_8.name())
            } catch (_: Exception) {
                rawUri
            }
            ProfilePhotoCropScreen(
                sourceUri = sourceUri,
                profileStore = profileStore,
                onCancel = { navController.popBackStack() },
                onCropped = {
                    runCatching {
                        navController.getBackStackEntry(Routes.MAIN)
                            .savedStateHandle["profile_photo_updated"] = true
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            ),
            enterTransition = {
                fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 4 }
            }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            ItemDetailScreen(
                viewModel = viewModel,
                itemId = itemId,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.editItem(itemId)) }
            )
        }

        composable(
            route = Routes.EDIT_ITEM,
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            ),
            enterTransition = {
                fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 4 }
            }
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            AddItemScreen(
                viewModel = viewModel,
                itemId = itemId,
                embedded = false,
                bottomNavVisible = false,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
    }
}
