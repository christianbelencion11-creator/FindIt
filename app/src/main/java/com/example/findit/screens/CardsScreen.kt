package com.example.findit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findit.ui.components.BankCardFace
import com.example.findit.ui.components.EmptyState
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.CardFormat
import com.example.findit.viewmodel.BankCardViewModel

@Composable
fun CardsScreen(
    onBackClick: () -> Unit,
    onOpenCard: (Long) -> Unit,
    onCreateCard: () -> Unit,
    viewModel: BankCardViewModel = viewModel(factory = BankCardViewModel.Factory)
) {
    val cards by viewModel.allCards.collectAsState()
    val total by viewModel.totalBalance.collectAsState()
    // Which card currently shows its full number (only one at a time).
    var revealedId by remember { mutableStateOf<Long?>(null) }

    PremiumScaffold(
        headerHeight = Dimensions.headerContentWithMenu,
        headerContent = { collapseFraction ->
            val secondaryAlpha = (1f - collapseFraction).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.md,
                        bottom = Spacing.sm
                    ),
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
                        text = "Wallet",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (cards.isEmpty()) {
                            "Cards and balances"
                        } else {
                            "${cards.size} card${if (cards.size == 1) "" else "s"}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f * secondaryAlpha),
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
            }
        }
    ) { scrollModifier ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(scrollModifier),
                contentPadding = PaddingValues(
                    start = Spacing.xl,
                    end = Spacing.xl,
                    top = Spacing.xl,
                    bottom = Spacing.xxxl + 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                item {
                    TotalBalanceHero(total = total, cardCount = cards.size)
                }

                if (cards.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.CreditCard,
                            message = "No cards yet.\nAdd a bank card or e-wallet to keep its number and balance handy."
                        )
                    }
                } else {
                    items(cards, key = { it.id }) { card ->
                        BankCardFace(
                            bankName = card.bankName,
                            cardType = card.cardType,
                            cardNumber = card.cardNumber,
                            holder = card.cardHolder,
                            balanceText = CardFormat.peso(card.balance),
                            colorIndex = card.colorIndex,
                            modifier = Modifier.clickable { onOpenCard(card.id) },
                            revealed = revealedId == card.id,
                            onToggleReveal = {
                                revealedId = if (revealedId == card.id) null else card.id
                            }
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = onCreateCard,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(Spacing.xl),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        }
    }
}

/** Dark "net worth" panel summing every card's balance. */
@Composable
private fun TotalBalanceHero(total: Double, cardCount: Int) {
    val shape = RoundedCornerShape(Dimensions.cardCornerRadius)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0F2A20), Color(0xFF05100C))
                )
            )
    ) {
        // Faint emerald glow in the corner.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-40).dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.18f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = CardFormat.peso(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = if (cardCount == 0) {
                        "Across all your cards"
                    } else {
                        "Across $cardCount card${if (cardCount == 1) "" else "s"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF6EE7B7),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
