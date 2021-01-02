plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(project(":agent"))
    testImplementation(project(":agent-config"))
    testImplementation(project(":agent-builder"))
    testImplementation(project(":agent-runtime"))
    testImplementation(project(":config"))
    testImplementation("junit:junit:4.12")


}

