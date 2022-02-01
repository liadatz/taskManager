val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "com.miniproject.ApplicationKt"))
        }
    }
}

group = "com.miniproject"
version = "1.0.0"
application {
    mainClass.set("com.miniproject.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.exposed:exposed-java-time:0.30.1")
    implementation("org.xerial:sqlite-jdbc:3.30.1")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

