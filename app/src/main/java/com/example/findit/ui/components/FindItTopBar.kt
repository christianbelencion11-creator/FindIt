package com.example.findit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.findit.R
import com.example.findit.ui.theme.Spacing

@Composable
fun FindItTopBar(
    modifier: Modifier = Modifier,
    title: String = "FindIt",
    subtitle: String? = null,
    showLogo: Boolean = true,
    logoSize: Dp = 52.dp,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(R.drawable.findit_logo_transparent),
                contentDescription = "FindIt logo",
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit
            )
        }
        Column(
            modifier = Modifier.padding(start = if (showLogo) Spacing.md else 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}
