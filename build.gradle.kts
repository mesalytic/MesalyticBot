plugins {
    id("java")
}

group = "org.virep"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.12")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("com.github.chocololat:Lavalink-Client:44164aae3f")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.github.Topis-Lavalink-Plugins:Topis-Source-Managers:2.0.6")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.4")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}