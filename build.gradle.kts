plugins {
    id("net.fabricmc.fabric-loom") version "1.17.11"
    kotlin("jvm") version "2.4.0"
}

group = "com.sraddons"
version = "1.7.2"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases/") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.19.2")
    implementation("net.fabricmc:fabric-language-kotlin:1.13.12+kotlin.2.4.0")
    implementation("net.fabricmc.fabric-api:fabric-api:0.149.1+26.1.2")

    // YACL - Yet Another Config Lib
    implementation("dev.isxander:yet-another-config-lib:3.9.4+26.1-fabric")

    // ModMenu
    compileOnly("com.terraformersmc:modmenu:18.0.0-beta.1")
}

loom {
    runConfigs.named("client") {
    }
}

tasks {
    processResources {
        val modVersion = project.version.toString()
        inputs.property("version", modVersion)
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
        }
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
        }
    }

    compileJava {
        sourceCompatibility = "25"
        targetCompatibility = "25"
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
