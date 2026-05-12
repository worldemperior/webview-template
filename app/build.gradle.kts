plugins {
    alias(libs.plugins.android.application)
}

android {

    namespace = "PACKAGE_PLACEHOLDER"

    compileSdk = 36

    signingConfigs {

        create("release") {

            storeFile =
                file("../release.jks")

            storePassword =
                System.getenv(
                    "KEYSTORE_PASSWORD"
                )

            keyAlias =
                System.getenv(
                    "KEY_ALIAS"
                )

            keyPassword =
                System.getenv(
                    "KEY_PASSWORD"
                )
        }
    }

    defaultConfig {

        applicationId =
            "PACKAGE_PLACEHOLDER"

        minSdk = 24
        targetSdk = 36

        versionCode =
            (System.currentTimeMillis() / 1000)
                .toInt()

        versionName =
            System.currentTimeMillis()
                .toString()
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            signingConfig =
                signingConfigs
                    .getByName("release")
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(
        "androidx.core:core-ktx:1.18.0"
    )

    implementation(
        "androidx.appcompat:appcompat:1.7.1"
    )

    implementation(
        "com.google.android.material:material:1.13.0"
    )

    implementation(
        "androidx.webkit:webkit:1.16.0"
    )

    implementation(
        "com.google.android.gms:play-services-ads:25.2.0"
    )

    implementation(
        "com.google.android.ump:user-messaging-platform:4.0.0"
    )
}