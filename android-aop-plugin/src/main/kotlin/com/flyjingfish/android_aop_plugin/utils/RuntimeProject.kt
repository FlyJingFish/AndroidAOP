package com.flyjingfish.android_aop_plugin.utils

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import java.io.File
import java.io.Serializable

data class RuntimeProject(
    val buildDir: File,
    val rootProjectBuildDir: File,
    val layoutBuildDirectory: File,
    val name: String,
    var androidConfig: AndroidConfig
): Serializable {
    companion object {
        fun get(project: Project): RuntimeProject {
            val runtimeProject = RuntimeProject(
                buildDir = project.buildDir,
                rootProjectBuildDir = project.rootProject.buildDir,
                layoutBuildDirectory = project.layout.buildDirectory.asFile.get(),
                androidConfig = AndroidConfig(),
                name = project.name
            )
            if (project != project.rootProject){
                project.afterEvaluate {
                    try {
                        val android = project.extensions.getByName("android") as BaseExtension
                        runtimeProject.androidConfig.setBootClasspath(android.bootClasspath)
                    } catch (_: Throwable) {
                    }
                }
            }
            return runtimeProject
        }

    }
}