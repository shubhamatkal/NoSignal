package com.shubham.nosignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shubham.nosignal.domain.model.SpeedTestResult
import com.shubham.nosignal.ui.SpeedTestViewModelNew
import com.shubham.nosignal.ui.components.SpeedTestPopup
import com.shubham.nosignal.ui.components.SpeedTestDetailPopup
import com.shubham.nosignal.utils.UnitFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpeedTestScreen(
    isDarkMode: Boolean,
    viewModel: SpeedTestViewModelNew = viewModel()
) {
    val backgroundColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val secondaryTextColor = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
    
    // State variables
    var showSpeedTestPopup by remember { mutableStateOf(false) }
    var showDetailPopup by remember { mutableStateOf(false) }
    var selectedResult by remember { mutableStateOf<SpeedTestResult?>(null) }
    var isRunningTest by remember { mutableStateOf(false) }
    var isUsingBits by remember { mutableStateOf(false) }
    
    // Observe ViewModel state
    val latestResult by viewModel.latestResult.collectAsState(initial = null)
    val pastResults by viewModel.recentResults.collectAsState(initial = emptyList())
    val networkType by viewModel.networkType.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Sticky Unit Toggle (Bits/Bytes) at top
        StickyUnitToggleSection(
            isUsingBits = isUsingBits,
            onToggleUnit = { isUsingBits = !isUsingBits },
            isDarkMode = isDarkMode
        )
        
        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Title Header Section
            item {
                TitleHeaderSection(textColor, secondaryTextColor, networkType, isConnected)
            }
            
            // Run Speed Test Button
            item {
                RunSpeedTestButton(
                    onClick = { 
                        if (isConnected) {
                            showSpeedTestPopup = true
                        }
                    },
                    isRunning = isRunningTest,
                    isEnabled = isConnected,
                    isDarkMode = isDarkMode
                )
            }
            
            // Latest Speed Test Summary Card
            latestResult?.let { result ->
                item {
                    LatestSpeedTestCard(
                        result = result,
                        isUsingBits = isUsingBits,
                        onClick = {
                            selectedResult = result
                            showDetailPopup = true
                        },
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode
                    )
                }
            }
            
            // Past Speed Tests Header
            item {
                Text(
                    text = "Past Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Past Speed Tests List
            if (pastResults.isNotEmpty()) {
                items(pastResults) { result ->
                    PastSpeedTestItem(
                        result = result,
                        isUsingBits = isUsingBits,
                        onClick = {
                            selectedResult = result
                            showDetailPopup = true
                        },
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode
                    )
                }
            } else {
                item {
                    EmptyStateMessage(
                        textColor = secondaryTextColor
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    
    // Speed Test Popup
    SpeedTestPopup(
        showPopup = showSpeedTestPopup,
        onDismiss = { showSpeedTestPopup = false },
        onTestCancel = { showSpeedTestPopup = false },
        onTestDone = { result ->
            // Result is already saved to DB in the popup
            showSpeedTestPopup = false
        },
        isDarkMode = isDarkMode,
        viewModel = viewModel
    )
    
    // Speed Test Detail Popup
    SpeedTestDetailPopup(
        showPopup = showDetailPopup,
        speedTestResult = selectedResult,
        onDismiss = { showDetailPopup = false },
        isDarkMode = isDarkMode
    )
}

@Composable
private fun StickyUnitToggleSection(
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit,
    isDarkMode: Boolean
) {
    val toggleBackgroundColor = if (isDarkMode) Color(0xFF222222) else Color(0xFFEDEDED)
    val selectedBackgroundColor = if (isDarkMode) Color(0xFF444444) else Color.White
    val selectedTextColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val unselectedTextColor = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    toggleBackgroundColor,
                    RoundedCornerShape(8.dp)
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Bits Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isUsingBits) selectedBackgroundColor else Color.Transparent
                    )
                    .clickableNoRipple { if (!isUsingBits) onToggleUnit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bits",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isUsingBits) selectedTextColor else unselectedTextColor
                )
            }
            
            // Bytes Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (!isUsingBits) selectedBackgroundColor else Color.Transparent
                    )
                    .clickableNoRipple { if (isUsingBits) onToggleUnit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bytes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (!isUsingBits) selectedTextColor else unselectedTextColor
                )
            }
        }
    }
}

@Composable
private fun TitleHeaderSection(
    textColor: Color,
    secondaryTextColor: Color,
    networkType: String,
    isConnected: Boolean
) {
    Column {
        Text(
            text = "Speed Test",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = "Measure your current network performance",
            fontSize = 16.sp,
            color = secondaryTextColor,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        // Network connection status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // Connection indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Connected to: $networkType",
                fontSize = 14.sp,
                color = if (isConnected) textColor else secondaryTextColor,
                fontWeight = if (isConnected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun RunSpeedTestButton(
    onClick: () -> Unit,
    isRunning: Boolean,
    isEnabled: Boolean,
    isDarkMode: Boolean
) {
    val buttonColors = if (isDarkMode) {
        ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color.White else Color(0xFF555555),
            contentColor = if (isEnabled) Color.Black else Color(0xFF999999)
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color(0xFF141414) else Color(0xFFCCCCCC),
            contentColor = if (isEnabled) Color.White else Color(0xFF666666)
        )
    }
    
    Button(
        onClick = onClick,
        enabled = isEnabled && !isRunning,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = buttonColors,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isEnabled) 4.dp else 0.dp)
    ) {
        if (isRunning) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (isDarkMode) Color.Black else Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                isRunning -> "Running Test..."
                !isEnabled -> "No Connection"
                else -> "Run Speed Test"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LatestSpeedTestCard(
    result: SpeedTestResult,
    isUsingBits: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val cardBackgroundColor = if (isDarkMode) Color(0xFF111111) else Color(0xFFF8F8F8)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Latest Speed Test",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Download Speed
                Column {
                    Text(
                        text = UnitFormatter.formatSpeed(result.downloadBps, isUsingBits),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Download",
                        fontSize = 12.sp,
                        color = secondaryTextColor
                    )
                }
                
                // Upload Speed
                Column {
                    Text(
                        text = UnitFormatter.formatSpeed(result.uploadBps, isUsingBits),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "Upload",
                        fontSize = 12.sp,
                        color = secondaryTextColor
                    )
                }
                
                // Latency
                Column {
                    Text(
                        text = "${result.latencyMs.toInt()} ms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "Latency",
                        fontSize = 12.sp,
                        color = secondaryTextColor
                    )
                }
            }
            
            Text(
                text = formatDateTime(result.timestamp),
                fontSize = 13.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun PastSpeedTestItem(
    result: SpeedTestResult,
    isUsingBits: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val cardBackgroundColor = if (isDarkMode) Color(0xFF111111) else Color(0xFFF8F8F8)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "↓ ${UnitFormatter.formatSpeed(result.downloadBps, isUsingBits)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "↑ ${UnitFormatter.formatSpeed(result.uploadBps, isUsingBits)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
                Text(
                    text = formatShortDateTime(result.timestamp),
                    fontSize = 12.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No past speed tests",
            fontSize = 16.sp,
            color = textColor
        )
    }
}

// Helper function to format full date and time
private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

// Helper function to format short date and time
private fun formatShortDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

// Extension function for clickable without ripple effect
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
} 