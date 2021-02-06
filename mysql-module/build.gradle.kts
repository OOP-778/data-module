plugins {
    java
}

dependencies {
    compileOnly("mysql:mysql-connector-java:8.0.21") {
        exclude("com.google.protobuf", "protobuf-java")
    }
    implementation(project(":common-sql"))
    implementation(project(":api"))

    compileOnly("com.zaxxer:HikariCP:4.0.1")
}
