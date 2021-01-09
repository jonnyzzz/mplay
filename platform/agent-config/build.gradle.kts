plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    testImplementation("junit:junit:4.12")
}
