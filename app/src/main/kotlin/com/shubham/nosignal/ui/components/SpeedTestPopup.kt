package com.shubham.nosignal.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shubham.nosignal.data.network.CloudflareSpeedTestEngine
import com.shubham.nosignal.domain.model.SpeedTestResult
import com.shubham.nosignal.ui.SpeedTestViewModelNew
import com.shubham.nosignal.utils.ISPDetector
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random

@Composable
fun SpeedTestPopup(
    showPopup: Boolean,
    onDismiss: () -> Unit,
    onTestCancel: () -> Unit,
    onTestDone: (SpeedTestResult) -> Unit,
    isDarkMode: Boolean = false,
    viewModel: SpeedTestViewModelNew = viewModel()
) {
    var isRunning by remember { mutableStateOf(true) }
    var isUsingBits by remember { mutableStateOf(false) }
    var finalResult by remember { mutableStateOf<SpeedTestResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Create speed test engine and ISP detector
    val speedTestEngine = remember { CloudflareSpeedTestEngine() }
    val ispDetector = remember { ISPDetector(viewModel.getApplication()) }
    
    // Observe real-time values from speed test engine
    val testProgress by speedTestEngine.progress.collectAsState()
    val currentDownloadSpeed by speedTestEngine.downloadSpeed.collectAsState()
    val currentUploadSpeed by speedTestEngine.uploadSpeed.collectAsState()
    val currentLatency by speedTestEngine.latency.collectAsState()
    val currentJitter by speedTestEngine.jitter.collectAsState()
    val currentPacketLoss by speedTestEngine.packetLoss.collectAsState()
    val downloadHistory by speedTestEngine.downloadHistory.collectAsState()
    val uploadHistory by speedTestEngine.uploadHistory.collectAsState()
    
    // Animation values
    val animatedProgress by animateFloatAsState(
        targetValue = testProgress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    // Run real Cloudflare speed test when popup is shown
    LaunchedEffect(showPopup) {
        if (showPopup && isRunning) {
            try {
                errorMessage = null
                
                // Run the real speed test
                val speedTestResults = speedTestEngine.runSpeedTest()
                
                // Get ISP information
                val ispName = try {
                    ispDetector.getISPName()
                } catch (e: Exception) {
                    "Unknown ISP"
                }
                
                val networkType = try {
                    ispDetector.getNetworkType()
                } catch (e: Exception) {
                    viewModel.getCurrentNetworkType()
                }
                
                // Calculate AIM scores
                val (streamingScore, gamingScore, rtcScore) = speedTestEngine.calculateAimScores(speedTestResults)
                
                // Create final result
                val result = SpeedTestResult(
                    timestamp = System.currentTimeMillis(),
                    networkType = networkType,
                    ispName = ispName,
                    downloadBps = speedTestResults.downloadBps,
                    uploadBps = speedTestResults.uploadBps,
                    unloadedLatencyMs = speedTestResults.latencyMs,
                    loadedDownloadLatencyMs = speedTestResults.loadedDownLatencyMs,
                    loadedUploadLatencyMs = speedTestResults.loadedUpLatencyMs,
                    unloadedJitterMs = speedTestResults.jitterMs,
                    loadedDownloadJitterMs = speedTestResults.loadedDownJitterMs,
                    loadedUploadJitterMs = speedTestResults.loadedUpJitterMs,
                    packetLossPercent = speedTestResults.packetLossPercent,
                    aimScoreStreaming = streamingScore,
                    aimScoreGaming = gamingScore,
                    aimScoreRTC = rtcScore
                )
                
                finalResult = result
                isRunning = false
                
            } catch (e: Exception) {
                errorMessage = "Speed test failed: ${e.message}"
                isRunning = false
            }
        }
    }
    
    // Cancel speed test when user cancels
    LaunchedEffect(isRunning) {
        if (!isRunning && showPopup) {
            speedTestEngine.cancel()
        }
    }
    
    if (showPopup) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            // Background with blur effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .blur(radius = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Main popup content
                AnimatedVisibility(
                    visible = showPopup,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300)
                    )
                ) {
                    SpeedTestContent(
                        isRunning = isRunning,
                        testProgress = animatedProgress,
                        currentDownloadSpeed = currentDownloadSpeed,
                        currentUploadSpeed = currentUploadSpeed,
                        currentLatency = currentLatency,
                        currentJitter = currentJitter,
                        currentPacketLoss = currentPacketLoss,
                        downloadHistory = downloadHistory,
                        uploadHistory = uploadHistory,
                        isUsingBits = isUsingBits,
                        onToggleUnit = { isUsingBits = !isUsingBits },
                        finalResult = finalResult,
                        errorMessage = errorMessage,
                        onCancel = {
                            isRunning = false
                            speedTestEngine.cancel()
                            onTestCancel()
                        },
                        onDone = {
                            finalResult?.let { result ->
                                viewModel.storeResult(result)
                                onTestDone(result)
                            }
                            onDismiss()
                        },
                        onRetry = {
                            isRunning = true
                            errorMessage = null
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedTestContent(
    isRunning: Boolean,
    testProgress: Float,
    currentDownloadSpeed: Double,
    currentUploadSpeed: Double,
    currentLatency: Double,
    currentJitter: Double,
    currentPacketLoss: Double,
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit,
    finalResult: SpeedTestResult?,
    errorMessage: String?,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit,
    isDarkMode: Boolean
) {
    val backgroundColor = if (isDarkMode) Color(0xFF111111) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val secondaryTextColor = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            SpeedTestHeader(
                isRunning = isRunning,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                onCancel = onCancel,
                onDone = onDone
            )
            
            if (isRunning) {
                // Running test view
                if (errorMessage != null) {
                    // Show error state
                    ErrorView(
                        errorMessage = errorMessage,
                        onRetry = onRetry,
                        onCancel = onCancel,
                        textColor = textColor,
                        isDarkMode = isDarkMode
                    )
                } else {
                    RunningTestView(
                        testProgress = testProgress,
                        currentDownloadSpeed = currentDownloadSpeed,
                        currentUploadSpeed = currentUploadSpeed,
                        currentLatency = currentLatency,
                        currentJitter = currentJitter,
                        currentPacketLoss = currentPacketLoss,
                        downloadHistory = downloadHistory,
                        uploadHistory = uploadHistory,
                        isUsingBits = isUsingBits,
                        onToggleUnit = onToggleUnit,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode
                    )
                }
            } else {
                // Summary view
                finalResult?.let { result ->
                    SummaryView(
                        result = result,
                        isUsingBits = isUsingBits,
                        onToggleUnit = onToggleUnit,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedTestHeader(
    isRunning: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    Column {
        // Drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRunning) "Running Network Speed Test..." else "Speed Test Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Connected • ${formatCurrentTime()}",
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            IconButton(
                onClick = if (isRunning) onCancel else onDone,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = if (isRunning) "Cancel" else "Done",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RunningTestView(
    testProgress: Float,
    currentDownloadSpeed: Double,
    currentUploadSpeed: Double,
    currentLatency: Double,
    currentJitter: Double,
    currentPacketLoss: Double,
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val cardBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF8F8F8)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Bits/Bytes toggle
        UnitToggle(
            isUsingBits = isUsingBits,
            onToggleUnit = onToggleUnit,
            isDarkMode = isDarkMode
        )
        
        // Download & Upload cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Download card
            SpeedCard(
                modifier = Modifier.weight(1f),
                title = "Download",
                speed = currentDownloadSpeed,
                speedHistory = downloadHistory,
                isUsingBits = isUsingBits,
                backgroundColor = cardBackgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                isDarkMode = isDarkMode
            )
            
            // Upload card
            SpeedCard(
                modifier = Modifier.weight(1f),
                title = "Upload",
                speed = currentUploadSpeed,
                speedHistory = uploadHistory,
                isUsingBits = isUsingBits,
                backgroundColor = cardBackgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                isDarkMode = isDarkMode
            )
        }
        
        // Latency, Jitter, Packet Loss row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Latency",
                value = "${currentLatency.toInt()} ms",
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            )
            MetricItem(
                label = "Jitter",
                value = "${String.format("%.1f", currentJitter)} ms",
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            )
            MetricItem(
                label = "Packet Loss",
                value = "${String.format("%.2f", currentPacketLoss)}%",
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Progress bar
        Column {
            Text(
                text = "Test Progress: ${(testProgress * 100).toInt()}%",
                fontSize = 14.sp,
                color = secondaryTextColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { testProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isDarkMode) Color.White else Color(0xFF141414),
                trackColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE0E0E0)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SpeedCard(
    modifier: Modifier = Modifier,
    title: String,
    speed: Double,
    speedHistory: List<Float>,
    isUsingBits: Boolean,
    backgroundColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = formatSpeed(speed, isUsingBits),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            // Mini line chart
            if (speedHistory.size > 1) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    drawSpeedChart(
                        speedHistory = speedHistory,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = secondaryTextColor
        )
    }
}

@Composable
private fun SummaryView(
    result: SpeedTestResult,
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean
) {
    val cardBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF8F8F8)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Bits/Bytes toggle
            UnitToggle(
                isUsingBits = isUsingBits,
                onToggleUnit = onToggleUnit,
                isDarkMode = isDarkMode
            )
        }
        
        item {
            // ISP & Network type card
            SummaryCard(
                title = "Network Information",
                backgroundColor = cardBackgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            ) {
                SummaryMetric("ISP", result.ispName)
                SummaryMetric("Connection", result.networkType)
                SummaryMetric("Test Date", formatDateTime(result.timestamp))
            }
        }
        
        item {
            // Speed results
            SummaryCard(
                title = "Speed Results",
                backgroundColor = cardBackgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            ) {
                SummaryMetric("Download", formatSpeed(result.downloadMbps, isUsingBits))
                SummaryMetric("Upload", formatSpeed(result.uploadMbps, isUsingBits))
            }
        }
        
        item {
            // Latency & Quality
            SummaryCard(
                title = "Connection Quality",
                backgroundColor = cardBackgroundColor,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor
            ) {
                SummaryMetric("Latency", "${result.latencyMs.toInt()} ms")
                SummaryMetric("Jitter", "${String.format("%.1f", result.unloadedJitterMs ?: 0.0)} ms")
                result.packetLossPercent?.let { loss ->
                    SummaryMetric("Packet Loss", "${String.format("%.2f", loss)}%")
                }
            }
        }
        
        // AIM Scores (if available)
        result.aimScoreStreaming?.let { streaming ->
            item {
                SummaryCard(
                    title = "AIM Quality Scores",
                    backgroundColor = cardBackgroundColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                ) {
                    SummaryMetric("Streaming", streaming)
                    result.aimScoreGaming?.let { gaming ->
                        SummaryMetric("Gaming", gaming)
                    }
                    result.aimScoreRTC?.let { rtc ->
                        SummaryMetric("Video Calls", rtc)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    backgroundColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = LocalContentColor.current.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = LocalContentColor.current
        )
    }
}

@Composable
private fun UnitToggle(
    isUsingBits: Boolean,
    onToggleUnit: () -> Unit,
    isDarkMode: Boolean
) {
    val toggleBackgroundColor = if (isDarkMode) Color(0xFF222222) else Color(0xFFEDEDED)
    val selectedBackgroundColor = if (isDarkMode) Color(0xFF444444) else Color.White
    val selectedTextColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val unselectedTextColor = if (isDarkMode) Color(0xFF999999) else Color(0xFF737373)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(toggleBackgroundColor, RoundedCornerShape(8.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Bits Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isUsingBits) selectedBackgroundColor else Color.Transparent)
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
                .background(if (!isUsingBits) selectedBackgroundColor else Color.Transparent)
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

// Helper functions
private fun DrawScope.drawSpeedChart(
    speedHistory: List<Float>,
    isDarkMode: Boolean
) {
    if (speedHistory.size < 2) return
    
    val chartColor = if (isDarkMode) Color.White else Color(0xFF141414)
    val strokeWidth = 2.dp.toPx()
    
    val maxValue = speedHistory.maxOrNull() ?: 1f
    val minValue = speedHistory.minOrNull() ?: 0f
    val range = maxValue - minValue
    
    val points = speedHistory.mapIndexed { index, value ->
        val x = (index.toFloat() / (speedHistory.size - 1)) * size.width
        val normalizedValue = if (range > 0) (value - minValue) / range else 0.5f
        val y = size.height - (normalizedValue * size.height)
        Offset(x, y)
    }
    
    for (i in 0 until points.size - 1) {
        drawLine(
            color = chartColor,
            start = points[i],
            end = points[i + 1],
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun formatSpeed(mbps: Double, isUsingBits: Boolean): String {
    return if (isUsingBits) {
        String.format("%.1f Mbps", mbps)
    } else {
        String.format("%.1f MB/s", mbps / 8.0)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

private fun formatCurrentTime(): String {
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(Date())
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    textColor: Color,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Speed Test Failed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = errorMessage,
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color.White else Color(0xFF141414),
                    contentColor = if (isDarkMode) Color.Black else Color.White
                )
            ) {
                Text("Retry")
            }
            
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = textColor
                )
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}