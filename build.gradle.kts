import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
}

group = "net.cdx"
version = "1.0.0"

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {

    // PAPER
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

    // KOTLIN

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.0")

    // DEPENDENCIES & LIBRARIES

    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.18.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.18.0")
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation("org.spongepowered:configurate-extra-kotlin:4.1.2")
    implementation("com.github.retrooper:packetevents-spigot:2.4.0")
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "net.cdx.bonusround.commandapi")
    relocate("com.github.retrooper.packetevents", "net.cdx.bonusround.packets.api")
    relocate("io.github.retrooper.packetevents", "net.cdx.bonusround.packets.impl")
    minimize()
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
    dependsOn(tasks.shadowJar)
}

tasks {
    runServer {
        jvmArgs("-Dcom.mojang.eula.agree=true")
        minecraftVersion("1.20.6")
    }
}

tasks.register<Copy>("buildToServer") {
    dependsOn(tasks.reobfJar)
    val jarFile = "build/libs/${project.name}-${project.version}-dev-all.jar"
    from(jarFile)
    into(System.getenv("OUTPUT_DIR"))
}

kotlin {
    jvmToolchain(21)
}