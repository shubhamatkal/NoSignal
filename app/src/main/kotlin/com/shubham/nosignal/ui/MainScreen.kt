package com.shubham.nosignal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shubham.nosignal.ui.screens.*
import com.shubham.nosignal.ui.screens.ConfigScreen

@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val backgroundColor = if (isDarkMode) Color(0xFF000000) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF141414)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Content (no header)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> SpeedScreen(isDarkMode = isDarkMode) // Speed (default)
                1 -> SignalStrengthScreen(isDarkMode = isDarkMode) // Signal Strength
                2 -> SpeedTestScreen(isDarkMode = isDarkMode) // Speed Test
                3 -> DataConsumptionScreen(isDarkMode = isDarkMode) // Daily Data
                4 -> ConfigScreen(isDarkMode = isDarkMode, onThemeChange = onThemeChange) // Config
            }
        }
        
        // Bottom Navigation
        BottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun BottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkMode: Boolean
) {
    val backgroundColor = if (isDarkMode) Color(0xFF000000) else Color.White
    val dividerColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFEDEDED)
    
    Column {
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dividerColor)
        )
        
        // Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Speed,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                isDarkMode = isDarkMode
            )
            BottomNavItem(
                icon = Icons.Outlined.SignalCellularAlt,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                isDarkMode = isDarkMode
            )
            BottomNavItem(
                icon = Icons.Outlined.NetworkCheck,
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                isDarkMode = isDarkMode
            )
            BottomNavItem(
                icon = Icons.Outlined.DataUsage,
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                isDarkMode = isDarkMode
            )
            BottomNavItem(
                icon = Icons.Outlined.Settings,
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                isDarkMode = isDarkMode
            )
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val selectedColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val unselectedColor = if (isDarkMode) Color(0xFF666666) else Color(0xFF737373)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else unselectedColor,
            modifier = Modifier.size(24.dp)
        )
    }
} 