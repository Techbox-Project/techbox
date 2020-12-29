plugins {
    kotlin("jvm") version "1.4.21"
}

repositories {
    jcenter()
}

dependencies {
    implementation("net.dv8tion:JDA:4.2.0_224")
    testImplementation("io.kotest:kotest-runner-junit5:4.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}