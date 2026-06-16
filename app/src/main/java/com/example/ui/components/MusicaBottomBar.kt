package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BottomBarBg
import com.example.ui.theme.DeepSoil
import com.example.ui.theme.MutedSoil

enum class BottomTab {
    HOME, LIBRARY
}

@Composable
fun MusicaBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(BottomBarBg)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavigationItem(
                selected = selectedTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                activeIcon = Icons.Filled.Home,
                inactiveIcon = Icons.Outlined.Home,
                contentDescription = "Home Player Screen",
                testTag = "tab_home"
            )

            BottomNavigationItem(
                selected = selectedTab == BottomTab.LIBRARY,
                onClick = { onTabSelected(BottomTab.LIBRARY) },
                activeIcon = Icons.AutoMirrored.Filled.QueueMusic,
                inactiveIcon = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = "Music Library Screen",
                testTag = "tab_library"
            )
        }
    }
}

@Composable
private fun BottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    contentDescription: String,
    testTag: String
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) DeepSoil else MutedSoil.copy(alpha = 0.6f),
        label = "iconColor"
    )

    val scaleFactor by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        label = "scale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .testTag(testTag)
            .size(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom subtle tactile feel, avoid standard heavy dark ripple
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scaleFactor)
        ) {
            Icon(
                imageVector = if (selected) activeIcon else inactiveIcon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            // Replicates the sleek minimalist look — no unrequested secondary text badges below indicators!
        }
    }
}
