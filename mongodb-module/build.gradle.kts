dependencies {
    implementation(project(":api"))
    compileOnly("org.mongodb:mongo-java-driver:3.12.2")
    compileOnly("com.google.code.gson:gson:2.8.6")
}