plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
}

version = "1.6"

var props: MutableMap<String, ProjectConfig> = hashMapOf()
loadProjects()

configureProject("api") {
    publish = false
}

configureProject("testing") {
    publish = false
}

configureProject("common-sql") {
    publish = false
}

subprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
        plugin("maven-publish")
    }

    repositories {
        jcenter()
        maven { setUrl("https://repo.codemc.org/repository/nms/") }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.8")
        annotationProcessor("org.projectlombok:lombok:1.18.8")
    }

    val config = props[name]

    tasks {
        register("cleanOut") {
            val directory = File("$projectDir/out/")
            if (directory.exists())
                directory.delete()
        }

        config?.let {
            shadowJar {
                archiveFileName.set("${it.name}.jar")
                destinationDirectory.set(file("out"))

                //relocate("org", "com.oop.datamodule.lib")
                relocate("google", "com.oop.datamodule.lib")
                relocate("com.google", "com.oop.datamodule.lib.google")
                relocate("com.mysql", "com.oop.datamodule.lib.mysql")
                relocate("com.mongodb", "com.oop.datamodule.lib.mongodb")
            }
        }

        build {
            dependsOn(findByName("cleanOut"))
            config?.let {
                if (it.publish) {
                    dependsOn(shadowJar)
                    dependsOn(publish)
                }
            }
        }
    }

    props[name]?.let { pc ->
        if (pc.publish) {
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
                        artifact(file("out/${pc.name}.jar"))

                        groupId = pc.group
                        artifactId = pc.artifact.replace("-module", "")
                        version = pc.version.toString()

                        println(pc.version.toString())
                        println(pc.artifact.replace("-module", ""))
                    }
                }
            }
        }
    }
}

tasks {
    register("generate-javadocs", Javadoc::class) {
        setDestinationDir(file("$buildDir/docs"))
        title = "$project.name $version API"

        subprojects.forEach { proj ->
            proj.tasks.withType<Javadoc>().forEach { javadocTask ->
                source += javadocTask.source
                classpath += javadocTask.classpath
                excludes += javadocTask.excludes
                includes += javadocTask.includes
            }
        }
    }
}

// << UTILS START >>
fun loadProjects() {
    for (children in childProjects.values)
        props[children.name.toLowerCase()] = ProjectConfig(children.name, version)
}

fun configureProject(name: String, apply: (ProjectConfig).() -> Unit) {
    props[name.toLowerCase()]?.let(apply)
}

data class ProjectConfig(
        val name: String,
        var outName: String = name,
        var publish: Boolean = true,
        var group: String = "com.oop.datamodule",
        var artifact: String = name,
        var version: Any
) {
    constructor(project: String, version: Any) : this(
            name = project, version = version
    )
}