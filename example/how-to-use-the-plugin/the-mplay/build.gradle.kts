plugins {
    kotlin("jvm")
    id("com.jonnyzzz.mplay") version "use-the-latest-version"
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

