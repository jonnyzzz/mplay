plugins {
    kotlin("jvm") version "1.4.10" apply false
}

subprojects {
    group = "com.jonnyzzz.mplay"

    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}
