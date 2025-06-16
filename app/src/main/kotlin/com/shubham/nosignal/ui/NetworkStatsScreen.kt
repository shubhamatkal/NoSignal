package com.shubham.nosignal.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.utils.UnitFormatter

/**
 * Main screen displaying real-time network statistics with graphs and controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkStatsScreen(
    viewModel: NetworkStatsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val isUsingBits by viewModel.isUsingBits.collectAsState()
    val speedSnapshots by viewModel.speedSnapshots.collectAsState()
    
    // Extract data for charts
    val downloadData = remember(speedSnapshots) { speedSnapshots.map { it.downloadSpeed } }
    val uploadData = remember(speedSnapshots) { speedSnapshots.map { it.uploadSpeed } }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Header
        StatusHeaderCard(isMonitoring = isMonitoring)
        
        // Unit toggle
        UnitToggleCard(
            isUsingBits = isUsingBits,
            onToggleUnit = viewModel::toggleUnit
        )
        
        // Current speed display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedCard(
                modifier = Modifier.weight(1f),
                title = "Download",
                speed = downloadSpeed,
                isUsingBits = isUsingBits,
                icon = Icons.Default.ArrowDownward,
                color = MaterialTheme.colorScheme.primary
            )
            
            SpeedCard(
                modifier = Modifier.weight(1f),
                title = "Upload", 
                speed = uploadSpeed,
                isUsingBits = isUsingBits,
                icon = Icons.Default.ArrowUpward,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // Live graphs
        if (isMonitoring && downloadData.isNotEmpty()) {
            GraphCard(
                title = "Download Speed",
                data = downloadData,
                color = MaterialTheme.colorScheme.primary,
                isUsingBits = isUsingBits
            )
            
            GraphCard(
                title = "Upload Speed",
                data = uploadData,
                color = MaterialTheme.colorScheme.secondary,
                isUsingBits = isUsingBits
            )
        }
        
        // Status information
        StatusCard(isMonitoring = isMonitoring, dataCount = speedSnapshots.size)
    }
}

@Composable
private fun StatusHeaderCard(
    isMonitoring: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Network Speed Monitor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isMonitoring) "Service is running continuously" else "Service is starting...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMonitoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Status indicator
            Icon(
                imageVector = if (isMonitoring) Icons.Default.CheckCircle else Icons.Default.Pending,
                contentDescription = if (isMonitoring) "Running" else "Starting",
                tint = if (isMonitoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun UnitToggleCard(
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Display Unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isUsingBits) "Showing in bits per second (bps)" else "Showing in bytes per second (B/s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bytes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!isUsingBits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isUsingBits,
                    onCheckedChange = { onToggleUnit() },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "Bits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUsingBits) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeedCard(
    modifier: Modifier = Modifier,
    title: String,
    speed: Float,
    isUsingBits: Boolean,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = UnitFormatter.formatSpeed(speed, isUsingBits),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun GraphCard(
    title: String,
    data: List<Float>,
    color: androidx.compose.ui.graphics.Color,
    isUsingBits: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (data.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(false)
                            setDragEnabled(false)
                            setScaleEnabled(false)
                            setPinchZoom(false)
                            legend.isEnabled = false
                            
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                setDrawAxisLine(true)
                                isEnabled = false
                            }
                            
                            axisLeft.apply {
                                setDrawGridLines(true)
                                setDrawAxisLine(true)
                                setLabelCount(5, false)
                            }
                            
                            axisRight.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val entries = data.mapIndexed { index, value ->
                            Entry(index.toFloat(), value)
                        }
                        
                        val dataSet = LineDataSet(entries, title).apply {
                            this.color = Color.parseColor("#" + Integer.toHexString(color.hashCode()).substring(2))
                            lineWidth = 2f
                            setDrawCircles(false)
                            setDrawValues(false)
                            setDrawFilled(true)
                            fillColor = this.color
                            fillAlpha = 50
                        }
                        
                        chart.data = LineData(dataSet)
                        chart.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Collecting data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    isMonitoring: Boolean,
    dataCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isMonitoring) {
                Text(
                    text = "• Updates every 500ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Showing last 60 seconds of data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Data points collected: $dataCount/120",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Foreground service is active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Data persists across app restarts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Starting network monitoring service...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNetworkStatsScreen() {
    MaterialTheme {
        NetworkStatsScreen()
    }
}
