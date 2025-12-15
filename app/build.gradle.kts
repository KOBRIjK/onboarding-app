plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // 🔥 ОБЯЗАТЕЛЬНО для Kotlin 2.0+
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.darkonboarding"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.darkonboarding"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.0")

    // XML theme (Material Components)
    implementation("com.google.android.material:material:1.13.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
}
