
val prepareDevelopment by tasks.creating {
    group = "build"
    doFirst {

        val gradlew = when {
            System.getProperty("os.name").contains("windows", ignoreCase = true) -> "gradlew.bat"
            else -> "./gradlew"
        }

        exec {
            //this step is needed to include all dependencies into the local maven repo
            workingDir = File(projectDir, "platform")
            environment("JAVA_HOME", System.getProperty("java.home"))
            commandLine(gradlew, "publishToMavenLocalForExamples")
        }
        exec {
            //this step is needed to include all dependencies into the local maven repo
            workingDir = File(projectDir, "gradle-plugin")
            environment("JAVA_HOME", System.getProperty("java.home"))
            commandLine(gradlew, "publishToMavenLocalForExamples")
        }
    }
}
