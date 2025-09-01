import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.net.URI

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.spm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations {
            val main by getting {
                // Choose the cinterop name
                cinterops.create("[cinteropName]")
            }
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-android:3.2.3")
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)


            implementation("dev.sargunv.maplibre-compose:maplibre-compose:0.10.4")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("com.squareup.okio:okio:3.5.0")


            implementation(project.dependencies.platform("io.ktor:ktor-bom:3.2.3"))
            implementation("io.ktor:ktor-client-core:3.2.3")
            implementation("io.ktor:ktor-client-cio:3.2.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
            implementation("io.ktor:ktor-client-logging:3.2.3")


            implementation("io.github.eltonkola:ku:0.0.5")

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.datetime)
            implementation(libs.materialKolor)
            implementation(libs.bundles.coil)
            implementation(libs.kermit)

            implementation(libs.bundles.koin)

            implementation("com.composables:icons-lucide:1.0.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-cio:3.2.3")
        }


        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.2.3")
        }

        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }



}

//TODO - ios lib
swiftPackageConfig {
    create("[cinteropName]") {

        dependency {
            remotePackageVersion(
                url = URI("https://github.com/maplibre/maplibre-gl-native-distribution.git"),
                products = { add("MapLibre") },
                version = "6.17.1",
            )
        }
    }
}

android {
    namespace = "com.eltonkola.everything"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.eltonkola.everything"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.eltonkola.everything.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.eltonkola.everything"
            packageVersion = "1.0.0"
        }

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs(
            "--add-opens",
            "java.desktop/java.awt.peer=ALL-UNNAMED"
        ) // recommended but not necessary

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}

// KSP Tasks
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
//    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}

// Trigger Common Metadata Generation from Native tasks
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

afterEvaluate {
    listOf("Debug", "Release").forEach { buildType ->
        tasks.named("ksp${buildType}KotlinAndroid").configure {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}