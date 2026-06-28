package com.example.findit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.findit.navigation.FindItNavHost
import com.example.findit.ui.theme.FindItTheme
import com.example.findit.util.ProfilePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val profilePreferences = remember { ProfilePreferences(applicationContext) }
            var themeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
            var profileImageUri by rememberSaveable {
                mutableStateOf(profilePreferences.getProfileImageUri())
            }
            var displayName by rememberSaveable {
                mutableStateOf(profilePreferences.getDisplayName())
            }
            var username by rememberSaveable {
                mutableStateOf(profilePreferences.getUsername())
            }
            var bio by rememberSaveable {
                mutableStateOf(profilePreferences.getBio())
            }
            val darkTheme = themeOverride ?: isSystemInDarkTheme()

            FindItTheme(darkTheme = darkTheme) {
                FindItNavHost(
                    isDarkTheme = darkTheme,
                    onDarkThemeChanged = { themeOverride = it },
                    profileImageUri = profileImageUri,
                    onProfileImageChanged = { uri ->
                        profileImageUri = uri
                        profilePreferences.setProfileImageUri(uri)
                    },
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    onProfileDetailsChanged = { newDisplayName, newUsername, newBio ->
                        displayName = newDisplayName
                        username = newUsername
                        bio = newBio
                        profilePreferences.setProfileDetails(
                            displayName = newDisplayName,
                            username = newUsername,
                            bio = newBio
                        )
                    }
                )
            }
        }
    }
}
