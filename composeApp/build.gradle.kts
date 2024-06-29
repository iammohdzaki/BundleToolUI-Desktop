import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.nativecoroutines)
}

group = "com.AabToApk"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm("desktop")
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            api(libs.kryo)
            implementation(libs.voyager.navigator)
            implementation(libs.koin.core)
            implementation(libs.kmmViewModel)
            implementation(libs.koin.compose)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AabToApk"
            packageVersion = "1.0.4"
            val iconsRoot = project.file("desktop-icons")
            macOS {
                iconFile.set(iconsRoot.resolve("launcher.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("launcher.ico"))
            }
            linux {
                iconFile.set(iconsRoot.resolve("launcher.png"))
            }
        }
    }
}
