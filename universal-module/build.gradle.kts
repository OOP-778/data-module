dependencies {
    compileOnly(project(":api"))
    implementation(project(":sqlite-module"))
    implementation(project(":mysql-module"))
    implementation(project(":json-module"))
    implementation(project(":postgresql-module"))
    implementation(project(":h2-module"))
    compileOnly(project(":common-sql"))
    implementation(project(":mongodb-module"))
    compileOnly("org.mongodb:mongo-java-driver:3.12.2")
    compileOnly("com.google.code.gson:gson:2.8.6")
}
