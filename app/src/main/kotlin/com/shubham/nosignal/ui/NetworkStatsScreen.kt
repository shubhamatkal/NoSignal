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
    val downloadData by viewModel.downloadData.collectAsState()
    val uploadData by viewModel.uploadData.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with service toggle
        ServiceControlCard(
            isMonitoring = isMonitoring,
            onToggleMonitoring = { enabled ->
                if (enabled) {
                    startSpeedMonitorService(context)
                } else {
                    stopSpeedMonitorService(context)
                }
            }
        )
        
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
        if (isMonitoring) {
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
        StatusCard(isMonitoring = isMonitoring)
    }
}

@Composable
private fun ServiceControlCard(
    isMonitoring: Boolean,
    onToggleMonitoring: (Boolean) -> Unit
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
                    text = if (isMonitoring) "Service is running" else "Service is stopped",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isMonitoring,
                onCheckedChange = onToggleMonitoring
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
                    text = "Display Units",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isUsingBits) "Showing bits per second" else "Showing bytes per second",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            TextButton(onClick = onToggleUnit) {
                Text(if (isUsingBits) "Switch to Bytes" else "Switch to Bits")
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = UnitFormatter.formatSpeed(speed, isUsingBits),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
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
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setTouchEnabled(false)
                        isDragEnabled = false
                        setScaleEnabled(false)
                        setPinchZoom(false)
                        
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                        
                        axisLeft.apply {
                            setDrawGridLines(true)
                            setDrawAxisLine(false)
                        }
                        
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = data.mapIndexed { index, value ->
                        val displayValue = if (isUsingBits) value * 8 else value
                        Entry(index.toFloat(), displayValue)
                    }
                    
                    val dataSet = LineDataSet(entries, title).apply {
                        this.color = color.hashCode()
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }
                    
                    chart.data = LineData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun StatusCard(isMonitoring: Boolean) {
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
                    text = "• Background service is active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Turn on monitoring to see real-time speeds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun startSpeedMonitorService(context: Context) {
    val intent = Intent(context, SpeedMonitorService::class.java).apply {
        action = SpeedMonitorService.ACTION_START_MONITORING
    }
    ContextCompat.startForegroundService(context, intent)
}

private fun stopSpeedMonitorService(context: Context) {
    val intent = Intent(context, SpeedMonitorService::class.java).apply {
        action = SpeedMonitorService.ACTION_STOP_MONITORING
    }
    context.startService(intent)
}
