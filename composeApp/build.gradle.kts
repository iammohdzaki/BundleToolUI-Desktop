import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    sourceSets {
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
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.navigation)
            // Navigation
            implementation(libs.androidx.compose.navigation)
            // Datastore
            implementation(libs.datastore)
            implementation(libs.datastore.prefs)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            
            // Ktor Client Engine
            implementation(libs.ktor.client.cio)
            
            // MCP Server module
            implementation(project(":mcp-server"))
            
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.logback.logging)
        }
        // UI test dependencies for JVM (Compose Desktop)
        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(libs.kotlin.test)
                implementation(libs.junit)
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("build/generated/buildConfig")
            resources.srcDir("src/jvmMain/composeResources")
        }
    }
}

version = "2.0.1"
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            
            // Required for Ktor CIO and HTTPS
            modules("java.instrument", "java.management", "java.naming", "java.sql", "jdk.crypto.ec")
            
            packageName = "AabToApk"
            packageVersion = project.version.toString()
            val iconsRoot = project.file("desktop-icons")
            macOS {
                iconFile.set(iconsRoot.resolve("launcher.icns"))
            }
            windows{
                iconFile.set(iconsRoot.resolve("launcher.ico"))
            }
            linux{
                iconFile.set(iconsRoot.resolve("launcher.png"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateBuildConfig")
}

abstract class GenerateBuildConfigTask : DefaultTask() {

    @get:Input
    abstract val appName: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputFile = outputDir.file("BuildConfig.kt").get().asFile
        outputFile.parentFile.mkdirs()

        val content = """
            package build

            object BuildConfig {
                const val APP_NAME = "${appName.get()}"
                const val VERSION = "${versionName.get()}"
            }
        """.trimIndent()

        outputFile.writeText(content)
        logger.lifecycle("✅ Generated ${outputFile.path}")
    }
}
tasks.register<GenerateBuildConfigTask>("generateBuildConfig") {
    appName.set("AabToApk")
    versionName.set(project.version.toString())
    outputDir.set(layout.buildDirectory.dir("generated/buildConfig"))
}
