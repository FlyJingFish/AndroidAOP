package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.getBuildDirectory
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class CleanWithCachePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.rootProject == project){
            cleanWithCache(project)
        }
    }

    private fun cleanWithCache(project:Project){
        try {
            project.tasks.register("aaaCleanKeepAopCache") {
                setTask(project)

                it.dependsOn(project.allprojects.mapNotNull { pro -> pro.tasks.findByName("clean") })

                it.doLast {
                    println("✅ All modules have been cleaned successfully!")
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun setTask(project: Project){
        project.subprojects.forEach { subproject ->
            val cacheDir = subproject.layout.buildDirectory.file("tmp/android-aop/").get().asFile
            val targetDir = File(project.getBuildDirectory().absolutePath,cacheDir.absolutePath.computeMD5())
            subproject.tasks.matching { cl -> cl.name == "clean" }.configureEach {cl ->

                cl.doFirst {
                    if (cacheDir.exists()){
                        if (targetDir.exists()){
                            targetDir.deleteRecursively()
                        }
                        copyDirectory(cacheDir,targetDir)
                    }
                }
                cl.doLast {
                    if (targetDir.exists()){
                        cacheDir.mkdirs()
                        copyDirectory(targetDir,cacheDir)
                        targetDir.deleteRecursively()
                    }
                }
            }
            setTask(subproject)
        }
    }

    private fun copyDirectory(source: File, target: File) {
        if (!target.exists()) target.mkdirs()

        source.listFiles()?.forEach { file ->
            val targetFile = File(target, file.name)
            if (file.isDirectory) {
                copyDirectory(file, targetFile)
            } else {
                file.copyTo(targetFile, overwrite = true)
            }
        }
    }
}