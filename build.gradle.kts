plugins {
    id("java")
}

group = "org.virep"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.10")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation ("com.sedmelluq:lavaplayer:1.3.77")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}