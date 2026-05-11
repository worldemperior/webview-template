plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {

    namespace = "com.template.webview"

    compileSdk = 36

    signingConfigs {

        create("release") {

            storeFile = file("../release.jks")

            storePassword =
                System.getenv("KEYSTORE_PASSWORD")

            keyAlias =
                System.getenv("KEY_ALIAS")

            keyPassword =
                System.getenv("KEY_PASSWORD")
        }
    }

    defaultConfig {

        applicationId = "com.template.webview"

        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            signingConfig =
                signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    buildFeatures {

        compose = true
    }
}

dependencies {

    implementation(
        "androidx.core:core-ktx:1.18.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.10.0"
    )

    implementation(
        "androidx.activity:activity-compose:1.13.0"
    )

    implementation(
        "androidx.compose.ui:ui:1.11.1"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview:1.11.1"
    )

    implementation(
        "androidx.compose.material3:material3:1.4.0"
    )

    implementation(
        "androidx.webkit:webkit:1.16.0"
    )

    implementation(
        "com.google.android.material:material:1.13.0"
    )

    implementation("com.google.android.gms:play-services-ads:25.2.0")
}