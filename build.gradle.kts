plugins {
    kotlin("jvm") version "1.4.21"
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("io.github.techbox.TechboxLauncher")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("net.dv8tion:JDA:4.2.0_224")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.google.guava:guava:30.1-jre")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.classgraph:classgraph:4.8.98")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")
    implementation("net.jodah:expiringmap:0.5.9")
    testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Required to fix IntelliJ bug
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}