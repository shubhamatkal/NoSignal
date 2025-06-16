package com.shubham.nosignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfigScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF000000) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val cardBackgroundColor = if (isDarkMode) Color(0xFF111111) else Color(0xFFF5F5F5)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Dark Mode Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = if (isDarkMode) "Dark Mode" else "Light Mode",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column {
                        Text(
                            text = "Dark Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Text(
                            text = if (isDarkMode) "Dark theme enabled" else "Light theme enabled",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
                        )
                    }
                }
                
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isDarkMode) Color.White else Color(0xFF141414),
                        checkedTrackColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE0E0E0),
                        uncheckedThumbColor = Color(0xFF999999),
                        uncheckedTrackColor = Color(0xFFCCCCCC)
                    )
                )
            }
        }
        
        // Spacer to push content to top
        Spacer(modifier = Modifier.weight(1f))
    }
} 