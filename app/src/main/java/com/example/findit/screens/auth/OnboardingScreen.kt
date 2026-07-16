package com.example.findit.screens.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findit.R
import com.example.findit.ui.theme.FindItGradientEnd
import com.example.findit.ui.theme.FindItGradientStart
import com.example.findit.ui.theme.Spacing
import kotlinx.coroutines.launch

private data class OnboardPage(
    val title: String,
    val body: String,
    @DrawableRes val imageRes: Int,
    val primaryCta: String
)

private val onboardPages = listOf(
    OnboardPage(
        title = "Put it away.\nFind it again.",
        body = "A calm home for all the little things you never want to lose track of.",
        imageRes = R.drawable.onboard_1_put_away,
        primaryCta = "Get Started"
    ),
    OnboardPage(
        title = "Never lose your\nthings again",
        body = "Store where you place your items and find them instantly when you forget.",
        imageRes = R.drawable.onboard_2_never_lose,
        primaryCta = "Next"
    ),
    OnboardPage(
        title = "Save items easily",
        body = "Add keys, wallet, or charger and note where you placed them. Forget where you left something? Just search — takes less than 5 seconds.",
        imageRes = R.drawable.onboard_3_save_easy,
        primaryCta = "Create account"
    )
)

@Composable
fun OnboardingScreen(
    onCreateAccount: () -> Unit,
    onAlreadyHaveAccount: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardPages.lastIndex
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(FindItGradientStart, FindItGradientEnd)
                    )
                )
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = Spacing.md)
        ) { page ->
            OnboardPageContent(
                page = onboardPages[page],
                useArchClip = page == 0
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(onboardPages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) scheme.primary
                            else Color(0xFFE2E6E3)
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Button(
                onClick = {
                    if (isLastPage) {
                        onCreateAccount()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                )
            ) {
                Text(
                    text = onboardPages[pagerState.currentPage].primaryCta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isLastPage) {
                OutlinedButton(
                    onClick = onAlreadyHaveAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = scheme.primary
                    ),
                    border = BorderStroke(1.dp, scheme.outline)
                ) {
                    Text(
                        text = "I already have an account",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardPageContent(
    page: OnboardPage,
    useArchClip: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    // Arch clip is Onboard 1 only (Figma crop); pages 2–3 keep rounded card.
    val imageShape = if (useArchClip) {
        RoundedCornerShape(
            topStart = 160.dp,
            topEnd = 160.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(24.dp)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold
            ),
            color = scheme.onBackground,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs, bottom = Spacing.sm)
        )
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.md)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = Spacing.sm)
                .clip(imageShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(imageShape),
                contentScale = ContentScale.Crop,
                alignment = if (useArchClip) Alignment.TopCenter else Alignment.Center
            )
        }
    }
}
