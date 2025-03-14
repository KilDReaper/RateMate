plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Apply the Google services plugin
}

android {
    namespace = "com.example.ratemate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ratemate"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose UI Libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase Bill of Materials (BoM) to manage Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:29.0.0")) // Use the latest BoM version

    // Firebase Authentication for login features
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Firestore (if you're using Firestore)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Analytics (optional, if you want to use Firebase Analytics)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase UI for Authentication (optional, provides UI for login screens)
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation ("androidx.appcompat:appcompat:1.4.0")  // Add or update this line if missing
    implementation ("androidx.core:core-ktx:1.7.0")

    implementation ("com.google.android.gms:play-services-maps:18.1.0") // Ensure the correct version
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.10")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
// For location-related services
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
     // Other Firebase dependencies (add if you're using other Firebase services)
    implementation ("com.google.android.gms:play-services-maps:17.0.1")
    implementation ("com.google.android.gms:play-services-location:17.0.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.11")
    implementation ("com.google.android.gms:play-services-location:17.0.0")
    implementation ("androidx.preference:preference:1.2.1")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.10")
    implementation ("com.google.android.material:material:1.8.0") // For toolbar, if you want one
    implementation ("androidx.appcompat:appcompat:1.5.1") // Toolbar support
     // Firebase Realtime Database (optional)
    // implementation("com.google.firebase:firebase-database-ktx")

}
