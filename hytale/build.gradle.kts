plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "br.com.lojasquare"
version = "2.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.hypixel.net/repository/Hytale/")
    flatDir {
        dirs("libs")
    }
}

dependencies {
    // Hytale Server API - provided at runtime
    compileOnly("com.hypixel.hytale:hytale-server-api:1.0.0")
    
    // Lombok for boilerplate reduction
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    
    // Gson for JSON parsing (usually included in Hytale, but we include just in case)
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes(
            "Plugin-Main" to "br.com.lojasquare.LojaSquarePlugin",
            "Plugin-Name" to "LojaSquare",
            "Plugin-Version" to project.version
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
