plugins {
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.compose") version "2.0.0" apply false
    id("com.android.application") version "8.6.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
    }
}

