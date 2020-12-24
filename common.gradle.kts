
allprojects {
    version = System.getenv("BUILD_NUMBER") ?: "SNAPSHOT"
    group = "com.jonnyzzz.mplay"

    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}
