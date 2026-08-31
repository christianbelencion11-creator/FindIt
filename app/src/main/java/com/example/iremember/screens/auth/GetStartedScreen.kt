package com.example.iremember.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iremember.R
import com.example.iremember.ui.components.AuthCardColor
import com.example.iremember.ui.components.AuthFieldBorderColor
import com.example.iremember.ui.components.AuthMutedColor
import com.example.iremember.ui.components.AuthTextColor
import com.example.iremember.ui.components.BrandWaveHeader
import com.example.iremember.ui.theme.IRememberGreenLight
import com.example.iremember.ui.theme.Spacing

/**
 * ARCHIVED — temporarily unused.
 *
 * The live first-run flow is [OnboardingScreen] (3-page pager) via Routes.GET_STARTED.
 * Keep this screen for later Figma / copy tweaks; do not wire it in IRememberNavHost until needed.
 */
@Deprecated(
    message = "Replaced by OnboardingScreen for now. Keep for later; not used in navigation.",
    replaceWith = ReplaceWith("OnboardingScreen")
)
@Composable
fun GetStartedScreen(
    onCreateAccount: () -> Unit,
    onAlreadyHaveAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthCardColor)
    ) {
        BrandWaveHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm, bottom = Spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    IRememberGreenLight.copy(alpha = 0.9f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(percent = 50)
                        )
                )
                Image(
                    painter = painterResource(R.drawable.iremember_get_started_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(176.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "Let's begin tracking items",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                ),
                fontWeight = FontWeight.Bold,
                color = AuthTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.md)
            )
            Text(
                text = "Keep track of your important items so you'll never lose them again.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = AuthMutedColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = Spacing.sm)
                    .padding(top = Spacing.sm, bottom = Spacing.xl)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FeatureColumn(
                    icon = Icons.Filled.Shield,
                    title = "Secure",
                    subtitle = "Your data is safe and protected.",
                    modifier = Modifier.weight(1f)
                )
                FeatureColumn(
                    icon = Icons.Filled.Notifications,
                    title = "Reminders",
                    subtitle = "Get notified when needed.",
                    modifier = Modifier.weight(1f)
                )
                FeatureColumn(
                    icon = Icons.Filled.Search,
                    title = "Easy Search",
                    subtitle = "Find your items in seconds.",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spacing.xxl))

            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Create New Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(AuthFieldBorderColor)
                )
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuthMutedColor,
                    modifier = Modifier.padding(horizontal = Spacing.md)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(AuthFieldBorderColor)
                )
            }

            OutlinedButton(
                onClick = onAlreadyHaveAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AuthTextColor
                )
            ) {
                Text(
                    text = "I Already Have an Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FeatureColumn(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(IRememberGreenLight, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AuthTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = AuthMutedColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xxs)
        )
    }
}
