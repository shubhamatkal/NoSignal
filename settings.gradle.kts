// settings.gradle.kts (root of NoSignal)
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.4.0"
        kotlin("android") version "1.9.24"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NoSignal"
include(":app")
