package com.nestorian87.eter.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

private val NavItemRippleShape = RoundedCornerShape(20.dp)

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
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
            .clip(NavItemRippleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (selected) colors.primary else colors.onSurfaceVariant,
        )

        Spacer(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(if (selected) 18.dp else 0.dp)
                .height(3.dp)
                .background(
                    color = if (selected) colors.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                ),
        )
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
            .clip(NavItemRippleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (selected) {
                        colors.primary.copy(alpha = 0.10f)
                    } else {
                        Color.Transparent
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.small)
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

        Spacer(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(if (selected) 18.dp else 0.dp)
                .height(3.dp)
                .background(
                    color = if (selected) colors.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                ),
        )
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
