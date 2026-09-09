plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "io.clearquote.clearquote_sdk_demo_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.clearquote.clearquote_sdk_demo_app"
        minSdk = 26
        targetSdk = 34
        versionCode = 33
        versionName = "4.12"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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

// cq-android-sdk POM lists BOM-managed deps with literal version "null".
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.version == "null") {
            when {
                requested.group.startsWith("androidx.compose") -> {
                    // Versions aligned with compose-bom:2026.06.01
                    val version = when (requested.name) {
                        "material-icons-core", "material-icons-extended" -> "1.7.8"
                        else -> "1.11.4"
                    }
                    useVersion(version)
                    because("SDK POM embeds null versions for Compose BOM-managed artifacts")
                }
                requested.name == "firebase-analytics-ktx" -> {
                    useVersion("22.5.0")
                    because("SDK POM embeds null version for Firebase BOM-managed artifact")
                }
            }
        }
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

    // Firebase (aligned with SDK 3.0.7)
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")

    // Compose BOM — required by cq-android-sdk:3.0.7 (and fixes null POM versions)
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-core")

    // Maven local
    implementation("io.clearquote.assessment.cq_sdk:cq-android-sdk:3.1.1@aar") { isTransitive = true }

    // Leak canary
    // debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.14")
}