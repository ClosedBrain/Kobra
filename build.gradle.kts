import org.jetbrains.kotlin.daemon.common.configureDaemonJVMOptions

plugins {
    kotlin("jvm") version "2.2.0"
    //id("com.closedbrain.dynamic") version "1.0.0"
}

group = "com.closedbrain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
    implementation("com.closedbrain:dynamic:1.0.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()

    options {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Enable-Native-Access" to "ALL-UNNAMED"
        )
    }
}

kotlin {
    jvmToolchain(24)
}