plugins {
    kotlin("jvm")
    id("com.jonnyzzz.mplay") version "SNAPSHOT" // use real version
}

repositories {
    mavenCentral()
}

val theAppToRun by configurations.creating

dependencies {
    // we need the app dependency to specify the application classes,
    // it could be just a set of JAR files or event a classes directory
    implementation(project(":how-to-use-the-plugin:the-app"))

    //this is a shortcut to start the app with javaagent
    theAppToRun(project(":how-to-use-the-plugin:the-app"))
}

mplay {

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val runTheAppWithAgent by tasks.creating(JavaExec::class.java) {
    dependsOn(mplay.agentTask)
    group = "mplay"
    mainClass.set("com.jonnyzzz.mplay.example.howToUse.Main")

    doFirst {
        classpath += theAppToRun
        jvmArgs(mplay.agentTask.mplayAgentArgs.get())
    }
}
