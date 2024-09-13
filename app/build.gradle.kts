plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "io.clearquote.clearquote_sdk_demo_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.clearquote.clearquote_sdk_demo_app"
        minSdk = 26
        targetSdk = 34
        versionCode = 18
        versionName = "2.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            merges += "META-INF/LICENSE*.md"
        }
    }

    buildFeatures {
        // Enables the view binding
        viewBinding = true

        // Enables build config file
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")

    // Maven local
    implementation("io.clearquote.assessment.cq_sdk:cq-android-sdk:2.3.0@aar") { isTransitive = true }

    // Leak canary
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.14")
}