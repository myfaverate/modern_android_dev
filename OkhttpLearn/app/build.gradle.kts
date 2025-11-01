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
    namespace = "io.github.okhttplearn"
    compileSdk {
        version = release(version = 36)
    }

    defaultConfig {
        applicationId = "io.github.okhttplearn"
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
        buildConfig = true
        compose = true
    }
}

dependencies {

    // hilt 依赖
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // navigation3 runtime
    implementation(libs.androidx.navigation3.runtime.android)
    // navigation3 ui
    implementation("androidx.navigation3:navigation3-ui-android:1.0.0-beta01")

    // okhttp3
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.3.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // retrofit3
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // hilt-navigation-compose
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    // converter-kotlinx-serialization
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}