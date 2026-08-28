package com.example.findit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.findit.model.BankCardStyles
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.CardFormat
import com.example.findit.util.CardNetwork

/**
 * A premium payment-card face: brand-matched gradient, soft depth circles, a gold chip,
 * the card network badge (Visa / Mastercard / …), card number, holder and balance.
 * Reused by the wallet list and the live editor preview, so a card always looks the same
 * wherever it appears.
 *
 * The face paints with [BankCardStyles.resolveGradient] so an auto-colored card takes on
 * the real brand colors of [bankName]. Pass [revealed] = true (or wire [onToggleReveal])
 * to show the full [cardNumber]; otherwise it stays masked.
 */
@Composable
fun BankCardFace(
    bankName: String,
    cardType: String,
    cardNumber: String,
    holder: String,
    balanceText: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    revealed: Boolean = false,
    onToggleReveal: (() -> Unit)? = null
) {
    val stops = BankCardStyles.resolveGradient(colorIndex, bankName).map { Color(it) }
    val shape = RoundedCornerShape(22.dp)
    val network = CardFormat.network(cardNumber)
    val numberText = when {
        cardNumber.isBlank() -> CardFormat.masked("")
        revealed -> CardFormat.group(cardNumber)
        else -> CardFormat.masked(cardNumber)
    }
    val canReveal = onToggleReveal != null && cardNumber.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // real credit-card ratio, so it never looks lopsided
            .clip(shape)
            .background(Brush.linearGradient(stops))
    ) {
        // Soft depth: two translucent circles bleeding off the top-right corner.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-56).dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-48).dp, y = 40.dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Bank name + type on the left, the network badge on the right.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bankName.ifBlank { "Bank name" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = cardType.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 1.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (network != null) {
                    NetworkBadge(network)
                }
            }

            // Gold EMV chip, sitting where a real card's chip does.
            CardChip()

            // Card number + reveal (eye) toggle.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = numberText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (canReveal) {
                    IconButton(
                        onClick = { onToggleReveal?.invoke() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = if (revealed) "Hide card number"
                            else "Show card number",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Balance (left) + holder (right).
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = balanceText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (holder.isNotBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CARDHOLDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = holder.uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/** Small gold EMV-style chip. */
@Composable
private fun CardChip() {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 32.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF7E27A), Color(0xFFD9B75B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Two faint contact lines for a bit of chip texture.
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.xs),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x33000000))
                )
            }
        }
    }
}

/**
 * The payment-network mark drawn top-right, like a real card. Mastercard gets its dual
 * circles; the others use their wordmark so the card is instantly recognizable.
 */
@Composable
private fun NetworkBadge(network: CardNetwork) {
    when (network) {
        CardNetwork.MASTERCARD -> Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEB001B))
            )
            Box(
                modifier = Modifier
                    .offset(x = (-10).dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xF2F79E1B))
            )
        }
        CardNetwork.VISA -> Text(
            text = "VISA",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = Color.White,
            letterSpacing = 1.sp
        )
        CardNetwork.AMEX -> NetworkWordmark("AMEX")
        CardNetwork.JCB -> NetworkWordmark("JCB")
        CardNetwork.UNIONPAY -> NetworkWordmark("UnionPay")
    }
}

@Composable
private fun NetworkWordmark(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
        color = Color.White,
        letterSpacing = 0.5.sp
    )
}
