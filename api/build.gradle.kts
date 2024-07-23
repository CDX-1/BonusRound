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

kotlin {
    jvmToolchain(21)
}