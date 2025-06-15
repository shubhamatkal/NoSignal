package com.shubham.nosignal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.ui.NetworkStatsScreen
import com.shubham.nosignal.utils.Settings

class MainActivity : ComponentActivity() {
    
    private lateinit var settings: Settings
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMonitoringIfEnabled()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        settings = Settings(this)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startMonitoringIfEnabled()
            }
        } else {
            startMonitoringIfEnabled()
        }
        
        setContent {
            NoSignalApp()
        }
    }
    
    private fun startMonitoringIfEnabled() {
        if (settings.isMonitoringEnabled()) {
            val intent = Intent(this, SpeedMonitorService::class.java).apply {
                action = SpeedMonitorService.ACTION_START_MONITORING
            }
            ContextCompat.startForegroundService(this, intent)
        }
    }
}

@Composable
fun NoSignalApp() {
    MaterialTheme {
        Surface {
            NetworkStatsScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNoSignalApp() {
    NoSignalApp()
}
