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
    implementation("net.dv8tion:JDA:4.2.0_224")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
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