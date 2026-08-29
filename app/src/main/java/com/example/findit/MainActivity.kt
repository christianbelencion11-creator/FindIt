package com.example.findit

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.findit.navigation.FindItNavHost
import com.example.findit.ui.theme.FindItTheme
import com.example.findit.ui.theme.ThemeMode
import com.example.findit.util.ProfileStore

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivityLifecycle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d(TAG, "onCreate() called")
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        setContent {
            val profileStore = remember { ProfileStore(applicationContext) }
            val profile by profileStore.state.collectAsState()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Reload from disk every time the app comes back to foreground.
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        profileStore.reload()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            var themeModeName by rememberSaveable { mutableStateOf(ThemeMode.Auto.name) }
            val themeMode = ThemeMode.fromName(themeModeName)
            val darkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.Auto -> isSystemInDarkTheme()
            }

            FindItTheme(darkTheme = darkTheme) {
                FindItNavHost(
                    isDarkTheme = darkTheme,
                    themeMode = themeMode,
                    onThemeModeChanged = { themeModeName = it.name },
                    profileStore = profileStore,
                    profileImageUri = profile.imageUri,
                    displayName = profile.displayName,
                    username = profile.username,
                    bio = profile.bio,
                    fullName = profile.fullName,
                    birthday = profile.birthday,
                    family = profile.family,
                    phone = profile.phone,
                    location = profile.location,
                    onProfileImageChanged = profileStore::updateImage,
                    onProfileDetailsChanged = { update ->
                        profileStore.updatePersonalDetails(
                            displayName = update.displayName,
                            username = update.username,
                            bio = update.bio,
                            fullName = update.fullName,
                            birthday = update.birthday,
                            family = update.family,
                            phone = update.phone,
                            location = update.location
                        )
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d(TAG, "onDestroy() called")
    }
}
