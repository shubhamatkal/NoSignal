package com.shubham.nosignal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Main screen displaying real-time network statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkStatsScreen(
    viewModel: NetworkStatsViewModel = viewModel()
) {
    val networkStats by viewModel.networkStats
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Network Monitor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Network Statistics Cards
            NetworkStatCard(
                title = "Download Speed",
                value = viewModel.formatSpeed(networkStats.downloadSpeed),
                icon = Icons.Default.ArrowDownward,
                color = MaterialTheme.colorScheme.primary
            )
            
            NetworkStatCard(
                title = "Upload Speed",
                value = viewModel.formatSpeed(networkStats.uploadSpeed),
                icon = Icons.Default.ArrowUpward,
                color = MaterialTheme.colorScheme.secondary
            )
            
            NetworkStatCard(
                title = "Today's Data Usage",
                value = viewModel.formatDataUsage(networkStats.dailyDataUsed),
                icon = Icons.Default.DataUsage,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        // Status text
        Text(
            text = "Updates every second",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Reusable card component for displaying network statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkStatsScreenPreview() {
    MaterialTheme {
        Surface {
            // Preview with mock data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Network Monitor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                NetworkStatCard(
                    title = "Download Speed",
                    value = "125.3 KB/s",
                    icon = Icons.Default.ArrowDownward,
                    color = MaterialTheme.colorScheme.primary
                )
                
                NetworkStatCard(
                    title = "Upload Speed",
                    value = "45.7 KB/s",
                    icon = Icons.Default.ArrowUpward,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                NetworkStatCard(
                    title = "Today's Data Usage",
                    value = "156.2 MB",
                    icon = Icons.Default.DataUsage,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
