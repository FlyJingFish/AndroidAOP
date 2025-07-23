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
        fun getAfterEvaluate(project: Project): RuntimeProject {
            val runtimeProject = RuntimeProject(
                buildDir = project.getBuildDirectory(),
                rootProjectBuildDir = project.rootProject.getBuildDirectory(),
                layoutBuildDirectory = project.layout.buildDirectory.asFile.get(),
                androidConfig = AndroidConfig(),
                name = project.name
            )
            if (project != project.rootProject){
                project.afterEvaluate {
                    setBootClasspath(runtimeProject, project)
                }
                setBootClasspath(runtimeProject, project)
            }
            return runtimeProject
        }
        fun get(project: Project): RuntimeProject {
            val runtimeProject = RuntimeProject(
                buildDir = project.getBuildDirectory(),
                rootProjectBuildDir = project.rootProject.getBuildDirectory(),
                layoutBuildDirectory = project.layout.buildDirectory.asFile.get(),
                androidConfig = AndroidConfig(),
                name = project.name
            )
            if (project != project.rootProject){
                setBootClasspath(runtimeProject, project)
            }
            return runtimeProject
        }

        private fun setBootClasspath(runtimeProject: RuntimeProject,project: Project){
            try {
                val android = project.extensions.getByName("android") as BaseExtension?
                if (android != null){
                    runtimeProject.androidConfig.setBootClasspath(android.bootClasspath)
                }
            } catch (_: Throwable) {
            }
        }
    }
}