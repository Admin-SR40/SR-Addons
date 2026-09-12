plugins {
    id("net.fabricmc.fabric-loom") version "1.17.20"
    kotlin("jvm") version "2.4.20"
}

group = "com.sraddons"
version = "1.7.5"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases/") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:26.2")
    implementation("net.fabricmc:fabric-loader:0.19.5")
    implementation("net.fabricmc:fabric-language-kotlin:1.14.1+kotlin.2.4.20")
    implementation("net.fabricmc.fabric-api:fabric-api:0.160.0+26.2")

    // YACL - Yet Another Config Lib
    implementation("dev.isxander:yet-another-config-lib:3.9.6+26.2-fabric")

    // ModMenu
    compileOnly("com.terraformersmc:modmenu:20.0.2")

    testImplementation(kotlin("test"))
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

    test {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
