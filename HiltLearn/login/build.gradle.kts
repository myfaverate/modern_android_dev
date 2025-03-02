import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "edu.tyut.login"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        compose = true
        buildConfig = true
    }
}

dependencies {

    // hilt 依赖
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // okhttp logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.11.0"){
        exclude(group = "com.squareup.okhttp3", module = "okhttp")
    }

    // kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // implementation 'com.squareup.retrofit2:converter-kotlinx-serialization:latest.version'
    // converter-kotlinx-serialization
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")


    // lifecycle-viewmodel-compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // lifecycle-runtime-compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // navigation-compose
    implementation("androidx.navigation:navigation-compose:2.8.7")


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