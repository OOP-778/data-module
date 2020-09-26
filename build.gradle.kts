plugins {
    java
    id("com.github.johnrengelman.shadow") version "4.0.4"
    `maven-publish`
}

version = "1.5"

repositories {
    jcenter()
    maven { setUrl("https://repo.codemc.org/repository/nms/") }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")

    implementation("mysql:mysql-connector-java:8.0.21")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation(fileTree("./lib"))
}

tasks {
    register("cleanOut") {
        val directory = File("${project.projectDir}/out/")
        if (directory.exists())
            directory.delete()
    }

    register("jar-with-dep", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveFileName.set("data-module-full.jar")
        destinationDirectory.set(File("out"))

        from(project.configurations.runtimeClasspath.get(), project.sourceSets.main.orNull!!.output)
    }

    register("jar-without-dep", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveFileName.set("data-module.jar")
        destinationDirectory.set(File("out"))

        val config = project.configurations.runtimeClasspath.get().copyRecursive()
        config.dependencies.removeIf { !it.name.contains("gson") }

        from(project.sourceSets.main.orNull!!.output, config)
        relocate("com.google.gson", "com.oop.datamodule.gson")
    }

    build {
        dependsOn(findByName("cleanOut"))
        dependsOn(findByName("jar-with-dep"))
        dependsOn(findByName("jar-without-dep"))
        dependsOn(publish)
    }
}

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
        register("jar-with-dependencies", MavenPublication::class) {
            val directory = File("${project.projectDir}/out/")
            if (directory.exists() && directory.listFiles()?.size == 2)
                artifact(directory.listFiles()!!.filter { file -> file.nameWithoutExtension.endsWith("full") }[0])
            groupId = "com.oop"
            artifactId = "data"
            version = "${project.version}-full"
        }

        register("jar-without-dependencies", MavenPublication::class) {
            val directory = File("${project.projectDir}/out/")
            if (directory.exists() && directory.listFiles()?.size == 2)
                artifact(directory.listFiles()!!.filter { file -> !file.nameWithoutExtension.endsWith("full") }[0])
            groupId = "com.oop"
            artifactId = "data"
            version = "${project.version}"
        }
    }
}