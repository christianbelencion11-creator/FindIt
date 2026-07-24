package com.example.findit.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing

@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    PremiumScaffold(
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.md, bottom = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(
                    onClick = onBackClick,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.alpha(secondaryAlpha)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Last updated: July 19, 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f * secondaryAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
            }
        }
    ) { scrollModifier ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollModifier),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                end = Spacing.xl,
                top = Spacing.xl,
                bottom = Spacing.xxxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item { LocalDataWarningCard() }

            item {
                PolicySection(
                    title = "1. Who we are",
                    body = "IRemember (“we”, “the app”) is a personal organization app that helps you " +
                        "remember where you stored everyday items. This Privacy Policy explains what information " +
                        "is collected, how it is used, and your choices."
                )
            }
            item {
                PolicySection(
                    title = "2. Account information",
                    body = "When you create an account, we process your username, email address, and authentication " +
                        "credentials through Firebase Authentication and related Firebase/Google Cloud services. " +
                        "Account profile fields you choose to save (such as display name or bio) may be stored " +
                        "with your account for sign-in and profile features. We use this data to authenticate you, " +
                        "secure your account, and provide account recovery features (such as password reset)."
                )
            }
            item {
                PolicySection(
                    title = "3. Item data (local only)",
                    body = "Items you add—including names, locations, categories, notes, photos, reminders, " +
                        "and history—are stored locally on your device (for example in the app’s on-device database). " +
                        "IRemember does not upload your item inventory to IRemember cloud storage. " +
                        "Because items are local-only, uninstalling the app permanently deletes your items, photos, " +
                        "and history. They cannot be recovered from IRemember after uninstall."
                )
            }
            item {
                PolicySection(
                    title = "4. Permissions",
                    body = "Depending on features you use, IRemember may request:\n\n" +
                        "• Microphone — for voice search (speech is processed by your device’s speech recognition service).\n" +
                        "• Notifications — for item reminders.\n" +
                        "• Camera / photos — if you attach images to items.\n" +
                        "• Internet — for account sign-in, password recovery, and news headlines.\n\n" +
                        "You can revoke permissions in your device settings. Some features will stop working without them."
                )
            }
            item {
                PolicySection(
                    title = "5. Third-party services",
                    body = "We use third-party services that process data according to their own policies:\n\n" +
                        "• Google Firebase (Authentication, Firestore, Cloud Functions, Analytics as configured) " +
                        "for accounts and related backend features.\n" +
                        "• On-device / Google speech recognition when you use voice search.\n" +
                        "• External news RSS feeds when you open News (headlines are fetched over the network).\n\n" +
                        "We do not sell your personal information."
                )
            }
            item {
                PolicySection(
                    title = "6. Retention",
                    body = "• Account data is retained while your account exists and as needed to provide the service.\n" +
                        "• Local items remain until you delete them or uninstall the app.\n" +
                        "• In-app History entries are automatically cleared after 30 days.\n" +
                        "• Found-item retention on the device may also follow the app’s 30-day cleanup rules for certain found records."
                )
            }
            item {
                PolicySection(
                    title = "7. Uninstall and data loss",
                    body = "Uninstalling IRemember removes local app data on your device, including saved items, " +
                        "photos stored by the app, reminders, and history. IRemember cannot restore that local data " +
                        "after uninstall because it is not synced to IRemember servers. Your account credentials " +
                        "with Firebase may still exist until you delete the account through supported account flows " +
                        "or provider tools."
                )
            }
            item {
                PolicySection(
                    title = "8. Children’s privacy",
                    body = "IRemember is not directed at children under 13. We do not knowingly collect personal " +
                        "information from children under 13. If you believe a child has provided account information, " +
                        "contact us so we can take appropriate action."
                )
            }
            item {
                PolicySection(
                    title = "9. Changes to this policy",
                    body = "We may update this Privacy Policy from time to time. The “Last updated” date at the top " +
                        "of this screen will change when we do. Continued use of the app after updates means you " +
                        "acknowledge the revised policy."
                )
            }
            item {
                PolicySection(
                    title = "10. Contact",
                    body = "For privacy questions, contact the developer who published IRemember on Google Play, " +
                        "or open an issue on the project’s GitHub repository. " +
                        "If you host a public copy of this policy (for example GitHub Pages), use that URL in " +
                        "the Google Play Console Privacy Policy field."
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    body: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
