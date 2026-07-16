package com.example.findit.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.findit.data.repository.NewsRepository
import com.example.findit.model.NewsFeed
import com.example.findit.model.NewsItem
import com.example.findit.ui.components.HeaderIconButton
import com.example.findit.ui.components.PremiumScaffold
import com.example.findit.ui.theme.Dimensions
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.UiPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { NewsRepository() }
    val uiPreferences = remember(context) { UiPreferences(context) }
    val region = remember(context) { NewsRepository.detectLocalRegion(context) }

    var selectedFeed by remember {
        mutableStateOf(
            NewsFeed.entries.firstOrNull {
                it.name.equals(uiPreferences.getNewsFeed(), ignoreCase = true)
            } ?: NewsFeed.Local
        )
    }
    var items by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load(feed: NewsFeed = selectedFeed) {
        scope.launch {
            isLoading = true
            error = null
            when (val result = repository.fetch(feed, region)) {
                is NewsRepository.Result.Success -> {
                    items = result.items
                    error = null
                }
                is NewsRepository.Result.Error -> {
                    items = emptyList()
                    error = result.message
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedFeed) { load(selectedFeed) }

    val subtitle = when (selectedFeed) {
        NewsFeed.Local -> "Headlines from ${region.displayName}"
        NewsFeed.International -> "World & international headlines"
    }

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
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "News",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .alpha(secondaryAlpha)
                    )
                }
                TextButton(
                    onClick = { load() },
                    modifier = Modifier.alpha(secondaryAlpha),
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { scrollModifier ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollModifier)
        ) {
            NewsFeedTabs(
                selectedFeed = selectedFeed,
                onSelect = { feed ->
                    if (selectedFeed != feed) {
                        selectedFeed = feed
                        uiPreferences.setNewsFeed(feed)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.lg,
                        bottom = Spacing.md
                    )
            )

            when {
                isLoading && items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                error != null && items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )
                        Text(
                            text = error ?: "Something went wrong.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = Spacing.md)
                        )
                        TextButton(onClick = { load() }) {
                            Text("Try again")
                        }
                    }
                }
                else -> {
                    val navBottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.lg,
                            end = Spacing.lg,
                            top = Spacing.xs,
                            bottom = Spacing.xxxl + navBottom + Spacing.lg
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        if (error != null) {
                            item {
                                Text(
                                    text = error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = Spacing.sm)
                                )
                            }
                        }
                        itemsIndexed(items, key = { _, item -> item.id }) { _, article ->
                            NewsRow(
                                item = article,
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsFeedTabs(
    selectedFeed: NewsFeed,
    onSelect: (NewsFeed) -> Unit,
    modifier: Modifier = Modifier
) {
    val feeds = listOf(NewsFeed.Local, NewsFeed.International)
    val selectedIndex = feeds.indexOf(selectedFeed).coerceAtLeast(0)

    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        feeds.forEachIndexed { index, feed ->
            val label = when (feed) {
                NewsFeed.Local -> "Local"
                NewsFeed.International -> "World"
            }
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(feed) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = feeds.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun NewsRow(
    item: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            NewsThumbnail(imageUrl = item.imageUrl)
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = item.source,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.published.isNotBlank()) {
                    Text(
                        text = item.published,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsThumbnail(imageUrl: String?) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp)
            )
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    }
}
