package com.shubham.nosignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.shubham.nosignal.ui.NetworkStatsViewModel
import com.shubham.nosignal.utils.UnitFormatter

@Composable
fun SpeedScreen(
    isDarkMode: Boolean,
    viewModel: NetworkStatsViewModel = viewModel()
) {
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val isUsingBits by viewModel.isUsingBits.collectAsState()
    val speedSnapshots by viewModel.speedSnapshots.collectAsState()
    
    // Extract data for charts (only last 20 points)
    val downloadData = remember(speedSnapshots) { speedSnapshots.map { it.downloadSpeed } }
    val uploadData = remember(speedSnapshots) { speedSnapshots.map { it.uploadSpeed } }
    
    val backgroundColor = if (isDarkMode) Color(0xFF000000) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val secondaryTextColor = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Sticky Unit Toggle (Bits/Bytes) at top
        StickyUnitToggleSection(
            isUsingBits = isUsingBits,
            onToggleUnit = viewModel::toggleUnit,
            isDarkMode = isDarkMode
        )
        
        // Speed Section Header
        Text(
            text = "Speed",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        // Download Speed Card
        SpeedCard(
            title = "Download",
            speed = downloadSpeed,
            isUsingBits = isUsingBits,
            data = downloadData,
            textColor = textColor,
            secondaryTextColor = secondaryTextColor
        )
        
        // Upload Speed Card  
        SpeedCard(
            title = "Upload",
            speed = uploadSpeed,
            isUsingBits = isUsingBits,
            data = uploadData,
            textColor = textColor,
            secondaryTextColor = secondaryTextColor
        )
        
        // Fill remaining space
        Spacer(modifier = Modifier.weight(1f))
    }
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
private fun SpeedCard(
    title: String,
    speed: Float,
    isUsingBits: Boolean,
    data: List<Float>,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 20.dp)
    ) {
        // Title
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        
        // Speed Value
        Text(
            text = UnitFormatter.formatSpeed(speed, isUsingBits),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        
        // Last 20 ticks label
        Text(
            text = "Last 20 ticks",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Chart
        if (data.isNotEmpty()) {
            MinimalistChart(
                data = data,
                color = Color(0xFF737373)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Collecting data...",
                    fontSize = 12.sp,
                    color = secondaryTextColor
                )
            }
        }
    }
}

@Composable
private fun MinimalistChart(
    data: List<Float>,
    color: Color
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(false)
                setDragEnabled(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                legend.isEnabled = false
                
                // Hide all axes
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                
                // Remove borders and background
                setDrawBorders(false)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
            
            val dataSet = LineDataSet(entries, "").apply {
                this.color = android.graphics.Color.parseColor("#737373")
                lineWidth = 3f
                setDrawCircles(false)
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = android.graphics.Color.parseColor("#EDEDED")
                fillAlpha = 255
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    )
}

// Extension function for clickable without ripple
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    )
} 