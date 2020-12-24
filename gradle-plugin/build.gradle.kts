plugins {
    id("com.gradle.plugin-publish") version "0.12.0"
    `maven-publish`
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.71" // we use 1.3.x to avoid conflicts with Gradle
}

val pluginName = "mplay"

repositories {
    google()
    mavenCentral()
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "mplay-gradle"
        }
    }
}

gradlePlugin {
    testSourceSets( sourceSets.test.get())

    plugins {
        create(pluginName) {
            this.id = "mplay-gradle"
            id = "com.jonnyzzz.mplay"
            implementationClass = "com.jonnyzzz.mplay.gradle.MPlayPlugin"
        }
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect")) //to avoid versions clash
//  testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13")
}

apply(from = File(rootProject.projectDir, "../common.gradle.kts"))

pluginBundle {
    website = "https://jonnyzzz.com/mplay"
    description = "" +
            "Record method calls with telemetry and parameters " +
            "in a real application, than use the recorded data to " +
            "profile the appliaction part or generate a test code from " +
            "the caputered traces"

    (plugins) {
        pluginName {
            displayName = "Method Player"
            version = project.version.toString()
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

@Suppress("UnstableApiUsage")
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

