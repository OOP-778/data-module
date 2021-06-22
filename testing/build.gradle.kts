dependencies {
    implementation(project(":api"))
    implementation(project(":sqlite-module"))
    implementation(project(":mysql-module"))
    implementation(project(":json-module"))
    implementation(project(":common-sql"))
    implementation(project(":mongodb-module"))
    implementation(project(":postgresql-module"))
    implementation(project(":h2-module"))
    implementation("com.h2database:h2:1.4.200")

    compileOnly("org.mongodb:mongo-java-driver:3.12.2")
    compileOnly("com.google.code.gson:gson:2.8.6")
}
