plugins {
    kotlin("jvm")
    id("com.jonnyzzz.mplay") version "SNAPSHOT" // use real version
}

repositories {
    mavenCentral()
}

dependencies {
    //we need the app dependency to specify the application classes,
    //it could be just a set of JAR files or event a classes directory
    implementation(project(":how-to-use-the-plugin:the-app"))
}

mplay {

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
