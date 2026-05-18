plugins {
    id("fabric-loom") version "1.14-SNAPSHOT"
    kotlin("jvm") version "2.2.0"
    `maven-publish`
}

group = "com.sraddons"
version = "1.5.3"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases/") {
        name = "Xander Maven"
    }
    maven("https://jitpack.io") {
        content {
            excludeGroup("dev.isxander")
            excludeGroup("dev.isxander.yacl")
        }
    }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.10")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.1+kotlin.2.1.10")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    // YACL - Yet Another Config Lib
    modImplementation("dev.isxander:yet-another-config-lib:3.8.2+1.21.11-fabric")

    // ModMenu
    modCompileOnly("com.terraformersmc:modmenu:11.0.4")
}

loom {
    runConfigs.named("client") {
        isIdeConfigGenerated = true
        vmArgs.addAll(
            arrayOf(
                "-Dmixin.debug.export=true",
                "-Ddevauth.enabled=true",
                "-Ddevauth.account=main"
            )
        )
    }
}

val modVersion = project.version.toString()

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
        }
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        }
    }

    compileJava {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
