version = "unspecified"

dependencies {
    compileOnly(project(":api"))
    implementation("com.google.code.gson:gson:2.8.6")
    compileOnly("com.zaxxer:HikariCP:4.0.1")
}
