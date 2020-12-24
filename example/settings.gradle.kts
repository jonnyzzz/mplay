rootProject.name = "Method Player Examples"
//NOTE: you do not need that file real standalone examples

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.name == "com.jonnyzzz.mplay") {
                useModule(project(":gradle-plugin"))
            }
        }
    }
}


includeBuild("${rootProject.projectDir}/../gradle-plugin")


include(":how-to-use-the-plugin:the-app")
include(":how-to-use-the-plugin:the-mplay")
