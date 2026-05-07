plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "project.team36"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "project.team36"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    //Navigations
    implementation("androidx.navigation:navigation-compose:2.9.8")
    //MapBox
    implementation("com.mapbox.maps:android:11.19.0")
    implementation("com.mapbox.extension:maps-compose:11.19.0")
    implementation("com.mapbox.search:mapbox-search-android:2.14.0-beta.1")
    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // OkHttp for nettverk og Server-Sent Events (SSE)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    // Coroutines for asynkrone kall
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //Icons
    implementation("androidx.compose.material:material-icons-extended")
    // Ktor og SDK-klient
    implementation("io.modelcontextprotocol:kotlin-sdk-client:0.9.0")
    implementation(platform("io.ktor:ktor-bom:3.4.1"))         // styrer alle versjoner automatisk
    implementation("io.ktor:ktor-client-android")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-cio:3.4.3")

    // Mockito for vanlige enhetstester (Unit tests i test-mappen)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.12")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.compose.runtime:runtime:1.11.0")

    //Room database
    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")

    //Til enhetstester
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.14.9")
}