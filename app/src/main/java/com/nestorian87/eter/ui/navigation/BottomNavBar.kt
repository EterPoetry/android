package com.nestorian87.eter.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun BottomNavBar(
    currentTab: TopLevelDestination,
    onTabSelected: (TopLevelDestination) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentTab == destination
                val isCreate = destination == TopLevelDestination.CREATE

                if (isCreate) {
                    CreateNavItem(
                        label = stringResource(destination.labelRes),
                        selected = isSelected,
                        onClick = { onTabSelected(destination) },
                    )
                } else {
                    StandardNavItem(
                        label = stringResource(destination.labelRes),
                        icon = destination.icon,
                        selected = isSelected,
                        onClick = { onTabSelected(destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StandardNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (selected) 6.dp else 0.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
        )

        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Spacer(modifier = Modifier.size(0.dp))
        }
    }
}

@Composable
private fun RowScope.CreateNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (selected) 6.dp else 0.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (selected) {
                        colors.primary.copy(alpha = 0.16f)
                    } else {
                        colors.surfaceVariant
                    }
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.tertiary else colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    tint = colors.onPrimary,
                )
            }
        }

        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Spacer(modifier = Modifier.size(0.dp))
        }
    }
}

@EterScreenPreviews
@Composable
private fun BottomNavBarPreview() {
    EterPreview {
        BottomNavBar(
            currentTab = TopLevelDestination.SUBSCRIPTIONS,
            onTabSelected = {},
        )
    }
}
