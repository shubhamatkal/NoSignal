# NoSignal - Network Monitoring App

A modern Android app built with Kotlin and Jetpack Compose that provides real-time network monitoring functionality.

## Features

- **Live Upload/Download Speed**: Real-time monitoring of network speeds in KB/s or MB/s
- **Daily Data Usage**: Tracks mobile data consumption in MB/GB for the current day
- **Material 3 Design**: Modern, beautiful UI following Material Design principles
- **Clean Architecture**: Well-structured code with clear separation of concerns

## Architecture

The app follows clean architecture principles with three distinct layers:

### Data Layer (`data/`)
- `NetworkStatsManager.kt`: Handles polling TrafficStats API and manages daily reset logic using SharedPreferences

### Domain Layer (`domain/`)
- `NetworkStatsRepository.kt`: Exposes Flow<NetworkStats> for the UI layer with business logic

### UI Layer (`ui/`)
- `NetworkStatsViewModel.kt`: Manages UI state and consumes repository data
- `NetworkStatsScreen.kt`: Jetpack Compose screen with reactive UI updates

## Technical Details

- **Refresh Rate**: Updates every 1 second
- **Daily Reset**: Automatically resets counters at midnight using SharedPreferences
- **Lifecycle Aware**: Properly handles coroutine cancellation on ViewModel cleanup
- **No Root Required**: Uses standard Android APIs (TrafficStats)
- **Android 8+ Support**: Minimum SDK 24, compiled for SDK 35

## Dependencies

- Kotlin
- Jetpack Compose (1.8.2)
- Material 3 (1.2.1)
- AndroidX Libraries
- Lifecycle ViewModel Compose (2.9.1)
- Material Icons Extended (1.6.8)

## Permissions

- `INTERNET`: For network access
- `ACCESS_NETWORK_STATE`: For network state monitoring

## Key Features

### Real-time Speed Monitoring
- Tracks upload and download speeds using TrafficStats API
- Automatically converts between B/s, KB/s, and MB/s for optimal display
- Updates every second with smooth UI transitions

### Daily Data Tracking
- Stores start-of-day traffic values in SharedPreferences
- Automatically resets at midnight using date-based keys
- Handles device reboots by reinitializing values
- Displays data usage in appropriate units (KB, MB, GB)

### Clean Architecture Benefits
- **Testable**: Each layer can be independently tested
- **Maintainable**: Clear separation of concerns
- **Scalable**: Easy to add new features or modify existing ones
- **Reactive**: Uses Flow for reactive data streams

## Build Instructions

1. Open project in Android Studio
2. Sync Gradle dependencies
3. Build and run on device or emulator

```bash
./gradlew assembleDebug
```

## Usage

The app automatically starts monitoring network traffic when launched. No user interaction is required - all statistics are displayed in real-time on the main screen.

- **Download Speed**: Shows current download rate
- **Upload Speed**: Shows current upload rate  
- **Today's Usage**: Shows total data consumed since midnight

The app properly handles lifecycle events and will resume monitoring when brought back to foreground.
