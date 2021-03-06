plugins {
    java
}

dependencies {
    implementation(project(":common-sql"))
    compileOnly("org.postgresql:postgresql:42.2.18.jre6")
    implementation(project(":api"))
    compileOnly("com.zaxxer:HikariCP:4.0.1")
}
