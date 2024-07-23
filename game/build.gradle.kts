import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.paperweight)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runpaper)
}

group = "net.bonusround"
version = "1.0.0"

apply(from = "../repositories.gradle")

dependencies {
    implementation(project(":api"))

    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

    implementation(libs.bundles.kotlin)

    compileOnly(libs.bundles.jda)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.database)
    implementation(libs.bundles.mccoroutines)
    implementation(libs.bundles.config) {
        exclude("org.jetbrains.kotlin")
    }

    compileOnly(libs.placeholderapi)
    implementation(libs.commandapi)
    implementation(libs.packetevents)
    implementation(libs.inventoryframework)
    implementation(libs.nbtapi)
}

tasks.withType<ShadowJar> {
    relocate("dev.jorel.commandapi", "net.cdx.bonusround.commandapi")
    relocate("com.github.retrooper.packetevents", "net.cdx.bonusround.packets.api")
    relocate("io.github.retrooper.packetevents", "net.cdx.bonusround.packets.impl")
    relocate("com.github.stefvanschie.inventoryframework", "net.cdx.bonusround.gui.api")
    relocate("de.tr7zw.changeme.nbtapi", "net.cdx.bonusround.nbt.api")
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