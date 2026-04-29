package com.flyjingfish.android_aop_plugin.utils

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
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
            } catch (e: Throwable) {
                try {
                    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
                    val sdkComponents = androidComponents::sdkComponents.get()
//                    println("sdkDirectory: $sdkDirectory")
//                    val sdkDirectory: Directory? = sdkComponents.sdkDirectory.get()
                    val bootClasspath: List<RegularFile> = sdkComponents.bootClasspath.get()

//                    bootClasspath.forEach { println("bootClasspath: ${it.asFile.absolutePath}") }
                    runtimeProject.androidConfig.setBootClasspath(bootClasspath.map { it.asFile })
                } catch (e: Exception) {
//                    e.printStackTrace()
                }
            }
        }
    }
}