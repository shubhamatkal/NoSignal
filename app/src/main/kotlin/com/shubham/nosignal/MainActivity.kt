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
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.ui.MainScreen
import com.shubham.nosignal.utils.Settings

class MainActivity : ComponentActivity() {
    
    private lateinit var settings: Settings
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMonitoringService()
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
                startMonitoringService()
            }
        } else {
            startMonitoringService()
        }
        
        setContent {
            NoSignalApp()
        }
    }
    
    /**
     * Always start the monitoring service automatically
     */
    private fun startMonitoringService() {
        val intent = Intent(this, SpeedMonitorService::class.java).apply {
            action = SpeedMonitorService.ACTION_START_MONITORING
        }
        ContextCompat.startForegroundService(this, intent)
    }
}

// SF Pro Display Font Family
val SFProDisplayFontFamily = FontFamily(
    Font(R.font.sfprodisplayregular, FontWeight.Normal),
    Font(R.font.sfprodisplaymedium, FontWeight.Medium),
    Font(R.font.sfprodisplaybold, FontWeight.Bold)
)

@Composable
fun NoSignalApp() {
    var isDarkMode by remember { mutableStateOf(false) }
    
    MaterialTheme(
        colorScheme = if (isDarkMode) {
            darkColorScheme(
                primary = Color.White,
                background = Color(0xFF000000),
                surface = Color(0xFF111111)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF141414),
                background = Color.White,
                surface = Color.White
            )
        },
        typography = Typography(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = SFProDisplayFontFamily),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = SFProDisplayFontFamily),
            displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = SFProDisplayFontFamily),
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = SFProDisplayFontFamily),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = SFProDisplayFontFamily),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = SFProDisplayFontFamily),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = SFProDisplayFontFamily),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = SFProDisplayFontFamily),
            titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = SFProDisplayFontFamily),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = SFProDisplayFontFamily),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = SFProDisplayFontFamily),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = SFProDisplayFontFamily),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = SFProDisplayFontFamily),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = SFProDisplayFontFamily),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = SFProDisplayFontFamily)
        )
    ) {
        Surface(
            color = if (isDarkMode) Color(0xFF000000) else Color.White
        ) {
            MainScreen(
                isDarkMode = isDarkMode,
                onThemeChange = { isDarkMode = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNoSignalApp() {
    NoSignalApp()
}
