plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Removed: alias(libs.plugins.kotlin.compose)
}

apply(plugin = "com.google.gms.google-services") // Apply Firebase plugin correctly

android {
    namespace = "com.example.digitaldetox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.digitaldetox"
        minSdk = 26
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
        compose = false // Make sure Compose is turned off if not used
    }
}

dependencies {
    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.3.0")

    // Firebase Core Analytics (optional but recommended)
    implementation("com.google.firebase:firebase-analytics:21.3.0")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:22.1.2")




    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.generativeai.v060)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.generativeai)

    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
