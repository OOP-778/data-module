version = "unspecified"

dependencies {
    compileOnly("org.xerial:sqlite-jdbc:3.32.3.2")
    implementation(project(":common-sql"))
    implementation(project(":api"))

    compileOnly("com.zaxxer:HikariCP:4.0.1")
}
