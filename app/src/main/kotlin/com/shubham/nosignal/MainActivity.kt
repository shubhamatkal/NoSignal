package com.shubham.nosignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shubham.nosignal.ui.NetworkStatsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoSignalApp()
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
