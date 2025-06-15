# NoSignal
foss network monitoring tool

## 🚀 Fast Dev Loop

Speed up your Android development workflow with the `runnosignal` bash alias!

### Setup Instructions

1. The alias has been added to your `~/.bashrc` file. Reload your shell configuration:
   ```bash
   source ~/.bashrc
   ```

2. Make sure your Android device is connected and USB debugging is enabled:
   ```bash
   adb devices
   ```

### Usage

From anywhere in your terminal, simply run:
```bash
runnosignal
```

### What it does

The `runnosignal` alias automates your entire development workflow:

1. **🔨 Builds the app** - Runs `./gradlew installDebug` to compile and install
2. **🚀 Launches the app** - Uses `adb shell am start` to open NoSignal on your device
3. **📱 Shows filtered logs** - Tails logcat output filtered for NoSignal-related logs

The alias automatically:
- Navigates to the project directory if needed
- Handles build errors gracefully
- Filters logcat for relevant tags: `NoSignal`, `System.err`, `AndroidRuntime`, and `com.shubham.nosignal`
- Provides clear status updates with emojis

### Pro Tips

- Press `Ctrl+C` to stop the logcat tail
- The alias works from any directory - it will find your project automatically
- If the build fails, the alias stops and shows the error
- Perfect for rapid iteration during development
