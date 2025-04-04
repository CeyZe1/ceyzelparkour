import java.net.URI

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"

    id("io.freefair.lombok") version "8.13"
}

group = "net.ceyzel"
version = "1.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = URI("https://repo.papermc.io/repository/maven-public/")
    } // PaperMC
    maven { url = URI("https://oss.sonatype.org/content/groups/public/") }
    maven { url = URI("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("org.spongepowered:configurate-yaml:4.1.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(Pair("version", version))
    }
    filteringCharset = "UTF-8"
}

tasks.shadowJar {
    archiveFileName = "CeyzelParkour-${version}.jar"
}
