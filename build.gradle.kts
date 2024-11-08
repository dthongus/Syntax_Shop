plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.47.0.0")
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}