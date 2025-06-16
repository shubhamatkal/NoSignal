package com.shubham.nosignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.shubham.nosignal.ui.NetworkStatsViewModel
import com.shubham.nosignal.utils.UnitFormatter

@Composable
fun DashboardScreen(
    viewModel: NetworkStatsViewModel = viewModel()
) {
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val isUsingBits by viewModel.isUsingBits.collectAsState()
    val speedSnapshots by viewModel.speedSnapshots.collectAsState()
    
    // Extract data for charts (only last 20 points)
    val downloadData = remember(speedSnapshots) { speedSnapshots.map { it.downloadSpeed } }
    val uploadData = remember(speedSnapshots) { speedSnapshots.map { it.uploadSpeed } }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Unit Toggle (Bits/Bytes)
        UnitToggleSection(
            isUsingBits = isUsingBits,
            onToggleUnit = viewModel::toggleUnit
        )
        
        // Speed Section Header
        Text(
            text = "Speed",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF141414),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        
        // Download Speed Card
        SpeedCard(
            title = "Download",
            speed = downloadSpeed,
            isUsingBits = isUsingBits,
            data = downloadData,
            color = Color(0xFF737373)
        )
        
        // Upload Speed Card  
        SpeedCard(
            title = "Upload",
            speed = uploadSpeed,
            isUsingBits = isUsingBits,
            data = uploadData,
            color = Color(0xFF737373)
        )
        
        // Add bottom padding for navigation
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun UnitToggleSection(
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Color(0xFFEDEDED),
                    RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Bits Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isUsingBits) Color.White else Color.Transparent
                    )
                    .clickableNoRipple { if (!isUsingBits) onToggleUnit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bits",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isUsingBits) Color(0xFF141414) else Color(0xFF737373)
                )
            }
            
            // Bytes Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (!isUsingBits) Color.White else Color.Transparent
                    )
                    .clickableNoRipple { if (isUsingBits) onToggleUnit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bytes",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!isUsingBits) Color(0xFF141414) else Color(0xFF737373)
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
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF141414),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Speed Value
        Text(
            text = UnitFormatter.formatSpeed(speed, isUsingBits),
            style = MaterialTheme.typography.displayLarge,
            color = Color(0xFF141414),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Last 20 ticks label
        Text(
            text = "Last 20 ticks",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF737373),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Chart
        if (data.isNotEmpty()) {
            MinimalistChart(
                data = data,
                color = color
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Collecting data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF737373)
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
            .height(180.dp)
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