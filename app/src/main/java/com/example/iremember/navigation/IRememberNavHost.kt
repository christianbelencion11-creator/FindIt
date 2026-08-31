package com.example.iremember.navigation

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
import com.example.iremember.IRememberApplication
import com.example.iremember.auth.AccountRepository
import com.example.iremember.auth.LocalAccountStore
import com.example.iremember.auth.SecretRecoveryRepository
import com.example.iremember.screens.ItemDetailScreen
import com.example.iremember.screens.AboutScreen
import com.example.iremember.screens.AddItemScreen
import com.example.iremember.screens.CardEditorScreen
import com.example.iremember.screens.CardsScreen
import com.example.iremember.screens.HistoryScreen
import com.example.iremember.screens.MainScreen
import com.example.iremember.screens.NewsScreen
import com.example.iremember.screens.NoteEditorScreen
import com.example.iremember.screens.NotesScreen
import com.example.iremember.screens.PrivacyPolicyScreen
import com.example.iremember.screens.ProfilePhotoCropScreen
import com.example.iremember.screens.EditProfileScreen
import com.example.iremember.screens.SplashScreen
import com.example.iremember.screens.StatsBrowseMode
import com.example.iremember.screens.StatsBrowseScreen
import com.example.iremember.screens.auth.ChangePasswordScreen
import com.example.iremember.screens.auth.ForgotPasswordScreen
import com.example.iremember.screens.auth.OnboardingScreen
import com.example.iremember.screens.auth.LoginScreen
import com.example.iremember.screens.auth.RegisterScreen
import com.example.iremember.screens.auth.SetupPinScreen
import com.example.iremember.screens.auth.UnlockScreen
import com.example.iremember.util.AuthPreferences
import com.example.iremember.util.ProfilePreferences
import com.example.iremember.util.ProfileStore
import com.example.iremember.ui.theme.ThemeMode
import com.example.iremember.viewmodel.ItemViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun IRememberNavHost(
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
    onProfileDetailsChanged: (com.example.iremember.screens.ProfileDetailsUpdate) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as IRememberApplication
    val authPreferences = remember { AuthPreferences(context) }
    val accountStore = remember { LocalAccountStore(context) }
    val authRepository = remember {
        AccountRepository(
            authPreferences,
            accountStore,
            application.repository,
            application.noteRepository,
            application.bankCardRepository
        )
    }
    val recoveryRepository = remember { SecretRecoveryRepository(accountStore) }
    val viewModel: ItemViewModel = viewModel(
        factory = ItemViewModel.Factory
    )

    fun syncProfileForUser(user: AccountRepository.SignedInUser) {
        val resolvedUsername = user.displayName.takeIf { it.isNotBlank() }
            ?: ProfilePreferences.DEFAULT_USERNAME
        profileStore.syncFromAuthUser(
            firebaseUid = user.uid,
            displayName = resolvedUsername,
            username = resolvedUsername,
            photoUrl = user.photoUrl
        )
    }

    fun restoreItemOwnerFromSession() {
        val uid = authRepository.currentUid ?: authPreferences.getFirebaseUid()
        if (uid.isNotBlank()) {
            application.repository.setOwnerUid(uid)
            application.noteRepository.setOwnerUid(uid)
            application.bankCardRepository.setOwnerUid(uid)
        }
    }

    fun navigateToMainAfterAuth(user: AccountRepository.SignedInUser) {
        authPreferences.setHasSeenGetStarted(true)
        application.repository.setOwnerUid(user.uid)
        application.noteRepository.setOwnerUid(user.uid)
        application.bankCardRepository.setOwnerUid(user.uid)
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
                onCardsClick = {
                    navController.navigate(Routes.CARDS)
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
                onItemClick = { itemId -> navController.navigate(Routes.itemDetail(itemId)) },
                onAddItemClick = { navController.navigate(Routes.ADD_ITEM) }
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

        composable(
            route = Routes.ADD_ITEM,
            enterTransition = {
                fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
            },
            exitTransition = {
                fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 4 }
            }
        ) {
            AddItemScreen(
                viewModel = viewModel,
                embedded = false,
                bottomNavVisible = false,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
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
                },
                bottomNavVisible = false
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

        composable(Routes.CARDS) {
            CardsScreen(
                onBackClick = { navController.popBackStack() },
                onOpenCard = { cardId ->
                    navController.navigate(Routes.cardEditor(cardId))
                },
                onCreateCard = {
                    navController.navigate(Routes.cardEditor(0L))
                }
            )
        }

        composable(
            route = Routes.CARD_EDITOR,
            arguments = listOf(
                navArgument("cardId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            CardEditorScreen(
                cardId = cardId,
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
