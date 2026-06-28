package com.example.findit.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.findit.FindItApplication
import com.example.findit.screens.ItemDetailScreen
import com.example.findit.screens.MainScreen
import com.example.findit.screens.SplashScreen
import com.example.findit.screens.auth.LoginScreen
import com.example.findit.screens.auth.RegisterScreen
import com.example.findit.screens.auth.SetupPinScreen
import com.example.findit.screens.auth.UnlockScreen
import com.example.findit.util.AuthPreferences
import com.example.findit.util.ProfilePreferences
import com.example.findit.viewmodel.ItemViewModel
import com.example.findit.viewmodel.ItemViewModelFactory

@Composable
fun FindItNavHost(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    profileImageUri: String = "",
    onProfileImageChanged: (String) -> Unit = {},
    displayName: String = ProfilePreferences.DEFAULT_DISPLAY_NAME,
    username: String = ProfilePreferences.DEFAULT_USERNAME,
    bio: String = ProfilePreferences.DEFAULT_BIO,
    onProfileDetailsChanged: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authPreferences = remember { AuthPreferences(context) }
    val application = context.applicationContext as FindItApplication
    val viewModel: ItemViewModel = viewModel(
        factory = ItemViewModelFactory(application.repository)
    )

    fun syncProfileFromAuth() {
        val fullName = authPreferences.getFullName()
        val emailUsername = authPreferences.getEmail()
            .substringBefore("@")
            .ifBlank { ProfilePreferences.DEFAULT_USERNAME }
        onProfileDetailsChanged(fullName, emailUsername, bio)
    }

    fun navigateToMainAfterAuth() {
        syncProfileFromAuth()
        if (authPreferences.isPinSet()) {
            navController.navigate(Routes.MAIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.SETUP_PIN) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                authPreferences = authPreferences,
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
                onNavigateToMain = {
                    syncProfileFromAuth()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authPreferences = authPreferences,
                onLoginSuccess = { navigateToMainAfterAuth() },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authPreferences = authPreferences,
                onRegisterSuccess = { navigateToMainAfterAuth() },
                onNavigateToLogin = { navController.popBackStack() }
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
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.UNLOCK) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onItemClick = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                },
                isDarkTheme = isDarkTheme,
                onDarkThemeChanged = onDarkThemeChanged,
                profileImageUri = profileImageUri,
                onProfileImageChanged = onProfileImageChanged,
                displayName = displayName,
                username = username,
                bio = bio,
                onProfileDetailsChanged = onProfileDetailsChanged,
                onLogout = {
                    authPreferences.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
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
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
