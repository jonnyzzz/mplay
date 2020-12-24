
val prepareDevelopment by tasks.creating {
    group = "build"
    doFirst {
        exec {
            //this step is needed to include all dependencies into the local maven repo
            workingDir = File(projectDir, "platform")
            commandLine("./gradlew", "publishToMavenLocal")
        }
        exec {
            //this step is needed to include all dependencies into the local maven repo
            workingDir = File(projectDir, "gradle-plugin")
            commandLine("./gradlew", "publishToMavenLocal")
        }
    }
}
