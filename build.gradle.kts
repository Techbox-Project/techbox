import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.21"
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

configure<ApplicationPluginConvention> {
    mainClassName = "io.github.techbox.TechboxLauncher"
}


repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Base
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:atomicfu:0.15.0")
    implementation("net.dv8tion:JDA:4.2.0_225")

    // Core
    implementation("com.google.guava:guava:30.1-jre")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.classgraph:classgraph:4.8.98")
    implementation("com.sksamuel.hoplite:hoplite-core:1.3.13")
    implementation("com.sksamuel.hoplite:hoplite-json:1.3.13")

    // Database
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.jetbrains.exposed:exposed-core:0.25.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.25.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.34.1")
    implementation("org.postgresql:postgresql:42.2.18")

    // Utils
    implementation("me.xdrop:fuzzywuzzy:1.3.1")
    implementation("net.jodah:expiringmap:0.5.9")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    withType<ShadowJar> {
        project.configurations.implementation.isCanBeResolved = true
        configurations = listOf(project.configurations.implementation.get())
    }
}