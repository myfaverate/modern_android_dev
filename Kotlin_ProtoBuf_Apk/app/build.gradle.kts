import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "edu.tyut.kotlin_protobuf_apk"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.tyut.kotlin_protobuf_apk"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = JvmTarget.JVM_21.target
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {

    // hilt 依赖
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // converter-kotlinx-serialization
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")

    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.11.0"){
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }

    // navigation-compose
    implementation("androidx.navigation:navigation-compose:2.8.7")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // okhttp logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // kotlinx-serialization-protobuf
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.0")

    // hilt-navigation-compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


    // dataStore
    // datastore-preferences
    implementation(libs.datastore.preferences)
    // datastore
    implementation(libs.androidx.datastore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}