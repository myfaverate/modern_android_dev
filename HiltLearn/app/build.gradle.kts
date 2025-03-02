import com.android.apksig.internal.apk.v4.V4Signature
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val keyStorePropertiesFile: File = rootProject.file("keystore.properties")
val keyStoreProperties = Properties().apply { load(FileInputStream(keyStorePropertiesFile)) }

android {
    namespace = "edu.tyut.hiltlearn"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.tyut.hiltlearn"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a", "armeabi-v7a"))
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

    // TODO 优化
    implementation(project(":login"))


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

/*
import com.android.build.gradle.internal.dsl.DefaultConfig
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

/**
 * 美团walle不再推荐使用，无意义，无法跟上google的现代化速度
 * 远程服务器一键打包脚本已经上传到仓库，参考 CsjProject Main.kt 万能视频剪辑执行脚本 -> 一键打不通渠道的包，当然有能力可以继续优化，优化空间很大
 * val channel: String by project
 * ./gradlew assembleRelease -Pchannel=oppo
 * ./gradlew assembleRelease -Pchannel=huawei
 *
 * ./gradlew assembleDebug -Pchannel=oppo
 * ./gradlew assembleDebug -Pchannel=huawei
 */
val channel = project.findProperty("channel") as String? ?: "dev" // "oppo" // -> 切换这个渠道名称 -> 然后 assembleRelease 即可打出不同渠道的包，有几个渠道打包几次，可以写一个脚本完成打包
println("当前打包渠道: $channel") // 打印当前使用的渠道

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
}

val keyStorePropertiesFile: File = rootProject.file("keystore.properties")
val keyStoreProperties = Properties().apply { load(FileInputStream(keyStorePropertiesFile)) }

android {
    namespace = "com.intbuller.cutassist"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.intbuller.cutassist"
        minSdk = 26
        targetSdk = 35
        versionCode = 10001
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a", "armeabi-v7a"))
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
        debug {
            buildConfigField(type = "String", name = "channel", value = """"$channel"""")
        }
        release {
            buildConfigField(type = "String", name = "channel", value = """"$channel"""")
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
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
    buildFeatures{
        viewBinding = true // 禁止使用dataBinding
        buildConfig = true
    }
    // apk打包重命名
    applicationVariants.all{
        outputs.all{
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl){
                val config: DefaultConfig = project.android.defaultConfig
                val versionName: String = config.versionName ?: "1.0.0"
                this.outputFileName = "videoEdit-${channel}-${buildType.name}-v${versionName}.${outputFile.extension}"
            }
        }
    }
}
// ./gradlew generatePublicKey --no-configuration-cache
tasks.register("generatePublicKey"){
    group = "sign"
    // keytool -export -keystore intbuller_cutassist.keystore -alias cutassist -file release-cert.crt
    // openssl x509 -in release-cert.crt -inform DER -out release-cert.pem -outform PEM
    // openssl x509 -in release-cert.pem -pubkey -noout > public-key.pem
    // openssl rsa -in public-key.pem -pubin -text -noout
    doLast{
        val keyAlias: String = keyStoreProperties["keyAlias"] as String
        val keyPassword: String = keyStoreProperties["keyPassword"] as String
        val storeFile: File = file(keyStoreProperties["storeFile"] as String).apply { logger.warn("storeFile: $this") }
        println(keyAlias)
        exec{
            commandLine("keytool", "-export", "-keystore", storeFile.toString(), "-alias", keyAlias, "-file", "${rootDir}/release-cert.crt", "-storepass", keyPassword)
        }
        exec{
            commandLine("openssl", "x509", "-in", "${rootDir}/release-cert.crt", "-inform", "DER", "-out", "${rootDir}/release-cert.pem", "-outform", "PEM")
        }
        exec{
            commandLine("openssl", "x509", "-in", "${rootDir}/release-cert.pem", "-pubkey", "-noout", "-out", "${rootDir}/public-key.pem")
        }
        val outputStream = ByteArrayOutputStream()
        exec{
            val execSpec:  ExecSpec = commandLine("openssl", "rsa", "-in", "${rootDir}/public-key.pem", "-pubin", "-modulus", "-noout")
            execSpec.standardOutput = outputStream
        }
        outputStream.use {
            val publicKey: String = it.toString(Charsets.UTF_8).replaceFirst("Modulus=", "").lowercase()
            println("公钥 (16 进制) 512位: |$publicKey|")
        }
    }
}

dependencies {

    // 接入 talking data
    implementation(fileTree(mapOf("dir" to "${rootDir}/libs", "include" to listOf("*.jar", "*.aar"))))

    // 阿里oss sdk
    implementation(libs.oss.android.sdk)

    // 抖音平台开发能力sdk接入
    implementation(libs.opensdk.china.external)
    implementation(libs.opensdk.common)

    // retrofit2
    implementation(libs.retrofit)
    // converter-gson
    implementation(libs.converter.gson)

    // hilt 依赖
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // okhttp3 请求拦截器
    implementation(libs.com.squareup.okhttp3.logging.interceptor2)

    // MMkv
    implementation(libs.mmkv)

    // viewModels
    implementation(libs.androidx.activity.ktx)
    // activityViewModels
    implementation(libs.androidx.fragment.ktx)

    // glide
    implementation(libs.glide)

    // 轻量级视频播放器 exoplayer
    implementation(libs.androidx.media3.media3.exoplayer7)
    // 播放器界面
    implementation(libs.androidx.media3.media3.ui7)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // 支付宝SDK
    implementation(libs.com.alipay.sdk)
    // 微信支付SDK
    implementation(libs.wechat.sdk.android)

    // 命令式UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /**
     * Compose UI
     */
    // coil 库
    implementation(libs.coil)
    implementation(libs.coil.compose)
    // compose 相对布局
    implementation(libs.constraintlayout.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // compose Lottie
    implementation(libs.lottie.compose)
    // compose paging
    implementation(libs.androidx.paging.compose)


    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 内存泄露分析工具
    debugImplementation(libs.leakcanary.android)
}
 */