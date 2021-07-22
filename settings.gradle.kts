rootProject.name = "data-module"
fun discoverProjects(namingPolicy: (List<File>, File) -> String) {
    fun discoverProjects(parents: MutableList<File>, dir: File) {
        try {
            for (listFile in dir.listFiles { _ -> true }!!) {
                if (listFile.isDirectory) {
                    val newParents = mutableListOf(*parents.toTypedArray())
                    newParents.add(dir)

                    discoverProjects(newParents, listFile)
                    continue
                }

                if (listFile.nameWithoutExtension.startsWith("build.gradle") && !listFile.isDirectory) {
                    val toJoin = mutableListOf(*parents.map { it.name }.toTypedArray())
                    toJoin.add(dir.name)

                    val projectId = toJoin.joinToString(":")
                    println("Found project $projectId adding to the included")

                    // Include inside the project list
                    include(projectId)

                    // Set the name based of naming policy
                    project(":$projectId").name = namingPolicy(parents, dir)
                }
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    val projectDirs = rootDir.listFiles { file -> file.isDirectory }!!
    projectDirs.forEach {
        if (it.name.contentEquals(".idea") || it.name.contentEquals(".gradle")) return@forEach
        val parents = mutableListOf<File>()
        discoverProjects(parents, it)
    }
}

discoverProjects { parents, project ->
    if (parents.isEmpty()) project.name else
        "${parents.last().name}-${project.name.toLowerCase()}"
}
