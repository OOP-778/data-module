plugins {
    java
    id("com.github.johnrengelman.shadow") version "4.0.4"
    `maven-publish`
}

version = "1.0"

repositories {
    jcenter()
    maven { setUrl("https://repo.codemc.org/repository/nms/") }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")

    compileOnly(fileTree("./lib"))
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("oop-data")
        destinationDirectory.set(File("out"))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
        dependsOn(publish)
    }
}

tasks.findByName("publish")!!.mustRunAfter("shadowJar")

publishing {
    repositories {
        mavenLocal()
        if (project.hasProperty("mavenUsername")) {
            maven {
                credentials {
                    username = project.property("mavenUsername") as String
                    password = project.property("mavenPassword") as String
                }

                setUrl("https://repo.codemc.org/repository/maven-releases/")
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            val directory = File("/out/")
            artifact(directory.listFiles()[0]!!)
            groupId = "com.oop"
            artifactId = "data"
            version = project.version as String
        }
    }
}