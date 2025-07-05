import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.io.InputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val keyStorePropertiesFile: File = rootProject.file("keystore.properties")
val keyStoreProperties: Properties = FileInputStream(keyStorePropertiesFile).use { inputStream: InputStream ->
    Properties().apply {
        load(inputStream)
    }
}


android {

    ndkVersion = "29.0.13599879"

    namespace = "edu.tyut.webviewlearn"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.tyut.webviewlearn"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(elements = arrayOf<String>("arm64-v8a", "armeabi-v7a"))
        }
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++23"
            }
        }
    }

    signingConfigs {
        create("release"){
            keyAlias = keyStoreProperties["keyAlias"] as String
            keyPassword = keyStoreProperties["keyPassword"] as String
            storeFile = file(keyStoreProperties["storeFile"] as String).apply { logger.warn("storeFile: $this") }
            storePassword = keyStoreProperties["storePassword"] as String
            enableV4Signing = true
        }
    }

    buildTypes {
        release {

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")

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
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {

    // room
    implementation("androidx.room:room-runtime:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    // hilt 依赖
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // retrofit
    implementation(libs.retrofit2.retrofit)

    // logging-interceptor
    implementation(libs.logging.interceptor)

    // converter-kotlinx-serialization
    implementation(libs.converter.kotlinx.serialization)

    // kotlinx.serialization.json
    implementation(libs.kotlinx.serialization.json)
    // kotlinx.serialization.protobuf
    implementation(libs.kotlinx.serialization.protobuf)

    // navigation.compose.android
    implementation(libs.androidx.navigation.compose)

    // camerax
    implementation(libs.androidx.camera.core)
    implementation("androidx.camera:camera-video:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    // exoplayer
    implementation("androidx.media3:media3-exoplayer:1.7.1")

    // hiltViewModel
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0-alpha01")

    // dataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore:1.1.7")

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