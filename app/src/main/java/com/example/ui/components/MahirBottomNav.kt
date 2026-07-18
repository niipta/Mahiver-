package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.Dimens
import com.example.ui.theme.MahirColors
import com.example.util.rememberMahirHaptics

data class NavItem(val route: String, val icon: ImageVector, val label: String)

/**
 * 6-tab premium bottom navigation.
 *
 * Layout: Home • Syllabus • Mocks • Focus (gold) • Revision • More
 *
 * The central Focus tab is visually emphasised — its icon stays gold even when
 * not selected, mimicking a FAB-like anchor in the middle of the bar.
 */
@Composable
fun MahirBottomNavigation(navController: NavController, modifier: Modifier = Modifier) {
    val haptics = rememberMahirHaptics()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        NavItem("home", Icons.Rounded.Home, "Home"),
        NavItem("syllabus", Icons.AutoMirrored.Rounded.MenuBook, "Syllabus"),
        NavItem("analytics", Icons.Rounded.Quiz, "Stats"),
        NavItem("focus", Icons.Rounded.Timer, "Focus"),
        NavItem("revision", Icons.Rounded.Repeat, "Revision"),
        NavItem("more", Icons.Rounded.MoreHoriz, "More")
    )

    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "syllabus" -> 1
        "analytics" -> 2
        "focus" -> 3
        "revision" -> 4
        "more" -> 5
        else -> -1
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.spacingLg),
        shape = RoundedCornerShape(Dimens.bottomNavRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = Dimens.bottomNavElevation
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(horizontal = Dimens.spacingXs, vertical = Dimens.spacingSm)
        ) {
            val itemWidth = maxWidth / items.size
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedIndex >= 0) itemWidth * selectedIndex else 0.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "navIndicator"
            )

            // Animated Indicator Pill
            if (selectedIndex >= 0) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .height(44.dp)
                        .background(
                            MahirColors.gold().copy(alpha = 0.15f),
                            RoundedCornerShape(Dimens.pillRadius)
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val isFocus = item.route == "focus"
                    Column(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (!isSelected) {
                                        haptics.tap()
                                        navController.navigate(item.route) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected || isFocus) MahirColors.gold() else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(if (isFocus) 24.dp else 20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isSelected || isFocus) MahirColors.gold() else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

