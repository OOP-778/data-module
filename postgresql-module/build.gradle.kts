plugins {
    java
}

dependencies {
    implementation(project(":common-sql"))
    implementation("org.postgresql:postgresql:42.2.18.jre6")
    implementation(project(":api"))
}
