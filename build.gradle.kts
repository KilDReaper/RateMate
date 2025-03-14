plugins {
    // Apply the Android application plugin at the project level
    alias(libs.plugins.android.application) apply false
    // Apply the Kotlin plugin at the project level
    alias(libs.plugins.kotlin.android) apply false
    // Apply the Kotlin Compose plugin at the project level (if needed)
    alias(libs.plugins.kotlin.compose) apply false
    // Google Services plugin for Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()  // Google's Maven repository for Firebase and Android dependencies
        mavenCentral()  // Central repository for other dependencies
    }
}
