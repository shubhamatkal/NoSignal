# UI Components

This directory contains reusable UI components for the NoSignal app.

## SpeedTestPopup

A full-screen modal popup component for running speed tests with the following features:

### Features
- **Two States**: Running test view and summary view
- **Animated Entry**: Slides in from bottom with spring animation
- **Blurred Background**: Background content is blurred when popup is shown
- **Live Updates**: Real-time speed, latency, jitter, and packet loss updates
- **Line Charts**: Mini line charts showing speed fluctuations for download/upload
- **Bits/Bytes Toggle**: Switch between bits and bytes display
- **Progress Bar**: Visual progress indicator during test
- **Summary View**: Scrollable summary with all test results
- **Database Integration**: Automatically saves results to Room DB

### Usage
```kotlin
SpeedTestPopup(
    showPopup = showPopup,
    onDismiss = { showPopup = false },
    onTestCancel = { showPopup = false },
    onTestDone = { result -> 
        // Handle test completion
        showPopup = false 
    },
    isDarkMode = isDarkMode,
    viewModel = speedTestViewModel
)
```

### Implementation Details
- Uses dummy data simulation for demonstration
- Integrates with existing SpeedTestViewModel
- Saves results to Room database (keeps only last 5 records)
- Supports both light and dark themes
- Material Design 3 components
- Smooth animations and transitions 