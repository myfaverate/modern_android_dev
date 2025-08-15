import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "edu.tyut.helloktorfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.tyut.helloktorfit"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin.compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
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

    // ktorFit 依赖
    implementation(libs.ktorfit.lib.light)

    // hilt-navigation-compose
    implementation(libs.androidx.hilt.navigation.compose)


    // ktor
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.slf4j.android)
    // implementation(libs.ktor.serialization.kotlinx.protobuf)

    // compose 相对布局
    implementation(libs.constraintlayout.compose.android)

    // coil3
    implementation(libs.coil3)
    implementation(libs.coil3.okhttp)
    implementation(libs.coil3.svg)

    // paging3
    implementation(libs.androidx.paging.compose)

    // lifecycle-service
    implementation(libs.androidx.lifecycle.service)

    // media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)

    implementation(libs.androidx.navigation.compose)
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