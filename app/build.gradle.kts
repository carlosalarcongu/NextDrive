plugins {
    alias(libs.plugins.android.application)
    // Removed alias(libs.plugins.kotlin.android) as it conflicts with AGP 9.0 built-in Kotlin
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.carlosalarcongu.nextdrive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.carlosalarcongu.nextdrive"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
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
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
    ksp("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.osmdroid:osmdroid-android:6.1.18")


    // Robolectric (Test Local)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // Espresso, Compose Tests y MockK (Test Instrumentado)
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.10") // ¡Faltaba esta!
    debugImplementation("androidx.compose.ui:ui-test-manifest") // Necesario para aislar Compose en tests
}
