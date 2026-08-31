package com.example.iremember.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iremember.model.BankCardStyles
import com.example.iremember.ui.components.BankCardFace
import com.example.iremember.ui.components.HeaderIconButton
import com.example.iremember.ui.components.PremiumScaffold
import com.example.iremember.ui.theme.Dimensions
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.ui.theme.darkCardGradientFill
import com.example.iremember.ui.theme.darkSurfaceBorder
import com.example.iremember.ui.theme.isAppDarkTheme
import com.example.iremember.util.CardFormat
import com.example.iremember.viewmodel.BankCardViewModel

@Composable
fun CardEditorScreen(
    cardId: Long,
    onBackClick: () -> Unit,
    viewModel: BankCardViewModel = viewModel(factory = BankCardViewModel.Factory)
) {
    val existing by viewModel.cardById(cardId).collectAsState(initial = null)
    val context = LocalContext.current

    var bankName by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf(BankCardStyles.DEFAULT_TYPE) }
    var cardNumber by remember { mutableStateOf("") } // digits only
    var cardHolder by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(BankCardStyles.AUTO_COLOR) }
    var loaded by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(existing, cardId) {
        if (cardId > 0L && existing != null && !loaded) {
            existing?.let { card ->
                bankName = card.bankName
                cardType = card.cardType
                cardNumber = card.cardNumber
                cardHolder = card.cardHolder
                balanceText = when {
                    card.balance == 0.0 -> ""
                    card.balance % 1.0 == 0.0 -> card.balance.toLong().toString()
                    else -> card.balance.toString()
                }
                colorIndex = card.colorIndex
            }
            loaded = true
        } else if (cardId == 0L && !loaded) {
            loaded = true
        }
    }

    val parsedBalance = CardFormat.parseBalance(balanceText)
    val nameError = showErrors && bankName.isBlank()
    val numberError = showErrors && cardNumber.length < 4
    val balanceError = showErrors && parsedBalance == null
    val canSave = bankName.isNotBlank() && cardNumber.length >= 4 && parsedBalance != null

    val dark = isAppDarkTheme()
    val formShape = RoundedCornerShape(Dimensions.cardCornerRadius)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete card?") },
            text = { Text("This removes the card and its balance from your wallet on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCard(cardId) { onBackClick() }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

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
                        text = if (cardId > 0L) "Edit card" else "New card",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Bank number and balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.alpha(secondaryAlpha)
                    )
                }
                if (cardId > 0L) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete card",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Live preview so the user sees their card as they type.
                item {
                    BankCardFace(
                        bankName = bankName,
                        cardType = cardType,
                        cardNumber = cardNumber,
                        holder = cardHolder,
                        balanceText = CardFormat.peso(parsedBalance ?: 0.0),
                        colorIndex = colorIndex,
                        revealed = true
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = formShape,
                        border = if (dark) darkSurfaceBorder() else null,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (dark) 0.dp else Dimensions.cardElevation
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .darkCardGradientFill(dark)
                                .padding(Spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank / wallet name") },
                                placeholder = { Text("BPI, GCash, BDO…") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = nameError,
                                supportingText = if (nameError) {
                                    { Text("Please complete this field") }
                                } else null,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )

                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                BankCardStyles.types.forEach { type ->
                                    FilterChip(
                                        selected = cardType == type,
                                        onClick = { cardType = type },
                                        label = { Text(type, maxLines = 1, softWrap = false) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = CardFormat.group(cardNumber),
                                onValueChange = { cardNumber = CardFormat.sanitizeNumber(it) },
                                label = { Text("Card / account number") },
                                placeholder = { Text("1234 5678 9012 3456") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = numberError,
                                supportingText = if (numberError) {
                                    { Text("Enter at least the last 4 digits") }
                                } else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = balanceText,
                                onValueChange = { input ->
                                    // Digits and a single decimal point only.
                                    val cleaned = input.filter { it.isDigit() || it == '.' }
                                    val firstDot = cleaned.indexOf('.')
                                    balanceText = if (firstDot == -1) {
                                        cleaned
                                    } else {
                                        cleaned.substring(0, firstDot + 1) +
                                            cleaned.substring(firstDot + 1).replace(".", "")
                                    }
                                },
                                label = { Text("Balance (₱)") },
                                placeholder = { Text("0.00") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = balanceError,
                                supportingText = if (balanceError) {
                                    { Text("Enter a valid amount, e.g. 1500") }
                                } else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            OutlinedTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it },
                                label = { Text("Cardholder name (optional)") },
                                placeholder = { Text("Juan Dela Cruz") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    text = "Card color",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Auto matches your bank's real colors (BDO, GCash, BPI…).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                // Auto: paints from the typed bank's brand colors.
                                val autoSelected = colorIndex == BankCardStyles.AUTO_COLOR
                                val autoStops = BankCardStyles
                                    .resolveGradient(BankCardStyles.AUTO_COLOR, bankName)
                                    .map { Color(it) }
                                Box(
                                    modifier = Modifier
                                        .size(width = 52.dp, height = 34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.linearGradient(autoStops))
                                        .then(
                                            if (autoSelected) {
                                                Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.onSurface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                            } else Modifier
                                        )
                                        .clickable { colorIndex = BankCardStyles.AUTO_COLOR },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = "Auto (brand colors)",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                BankCardStyles.gradients.forEachIndexed { index, stops ->
                                    val selected = colorIndex == index
                                    Box(
                                        modifier = Modifier
                                            .size(width = 52.dp, height = 34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(stops.map { Color(it) })
                                            )
                                            .then(
                                                if (selected) {
                                                    Modifier.border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.onSurface,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                } else Modifier
                                            )
                                            .clickable { colorIndex = index }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                tonalElevation = if (dark) 0.dp else 4.dp,
                shadowElevation = if (dark) 0.dp else 8.dp,
                color = if (dark) Color.Transparent else MaterialTheme.colorScheme.surface,
                border = if (dark) darkSurfaceBorder() else null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .darkCardGradientFill(dark)
                ) {
                    Button(
                        onClick = {
                            if (!canSave) {
                                showErrors = true
                                Toast.makeText(
                                    context,
                                    "Please complete the required fields.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            viewModel.saveCard(
                                cardId = cardId,
                                bankName = bankName,
                                cardHolder = cardHolder,
                                cardNumber = cardNumber,
                                cardType = cardType,
                                balance = parsedBalance ?: 0.0,
                                colorIndex = colorIndex
                            ) { onBackClick() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (cardId > 0L) "Save changes" else "Add card",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                }
            }
        }
    }
}
