import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

// local.properties dosyasından API anahtarını okumak için
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.babelsoftware.lifetools"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.babelsoftware.lifetools"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-beta.6"

        // API anahtarını BuildConfigField olarak ekle
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Temel AndroidX ve Yaşam Döngüsü Kütüphaneleri
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Jetpack Compose Bağımlılıkları
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Compose - Bill of Materials (BOM)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Gemini API (Google AI Generative Language)
    // En güncel versiyonu kontrol et: https://github.com/google/generative-ai-android
    implementation(libs.google.ai.client.generativeai)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.compose.foundation:foundation:1.8.2")
    implementation("io.ktor:ktor-client-core:2.3.11") // En güncel versiyonları kontrol edin
    implementation("io.ktor:ktor-client-okhttp:2.3.11") // Android için önerilen motor
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation(libs.firebase.crashlytics.buildtools)

    implementation("com.github.jeziellago:compose-markdown:0.4.0")
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // only for debug and preview
    debugImplementation("androidx.test:core-ktx:1.6.1")

    // for Widget
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // Test Bağımlılıkları
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}