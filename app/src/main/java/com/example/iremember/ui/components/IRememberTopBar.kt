package com.example.iremember.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.iremember.R
import com.example.iremember.ui.theme.Spacing

@Composable
fun IRememberTopBar(
    modifier: Modifier = Modifier,
    title: String = "IRemember",
    subtitle: String? = null,
    showLogo: Boolean = true,
    logoHeight: Dp = 44.dp,
    logoWidth: Dp = 56.dp,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(R.drawable.iremember_logo),
                contentDescription = "IRemember logo",
                modifier = Modifier
                    .width(logoWidth)
                    .height(logoHeight),
                contentScale = ContentScale.Fit
            )
        }
        // Logo lockup already includes "IRemember" — avoid duplicate title when logo is shown.
        val showTitleText = !showLogo || (title != "IRemember" && title.isNotBlank())
        if (showTitleText || subtitle != null) {
            Column(
                modifier = Modifier.padding(start = if (showLogo) Spacing.md else 0.dp)
            ) {
                if (showTitleText) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = contentColor
                    )
                }
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
}
