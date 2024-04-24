package com.flyjingfish.android_aop_plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.util.GradleVersion
import java.io.File
import java.util.Locale

class AndroidAopPlugin : Plugin<Project> {
    private companion object {
        private const val ANDROID_EXTENSION_NAME = "android"
    }
    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val logger = project.logger

        project.extensions.add("androidAopConfig", AndroidAopConfig::class.java)

        val debugModeStr :String = project.properties["androidAop.debugMode"].toString()
        val debugMode = debugModeStr == "true"
        if (debugMode){
            if (!isApp) {
                logger.warn("Plugin ['android.aop'] 应该被用于 com.android.application 所在 module 下,打正式包时请注意将 androidAop.debugMode 设置为 false")
            }
            val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)

            val android = project.extensions.findByName(ANDROID_EXTENSION_NAME) as BaseExtension
            val variants = if (isApp or isDynamicLibrary) {
                (android as AppExtension).applicationVariants
            } else {
                (android as LibraryExtension).libraryVariants
            }

            variants.all { variant ->
                variant.outputs.all { output ->
                    val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
                    AndroidAopConfig.debug = androidAopConfig.debug
                    androidAopConfig.includes.forEach {
                        AndroidAopConfig.includes.add("$it.")
                    }
                    androidAopConfig.excludes.forEach {
                        AndroidAopConfig.excludes.add("$it.")
                    }
                    AndroidAopConfig.excludes.add(Utils.annotationPackage)
                    AndroidAopConfig.excludes.add(Utils.corePackage)
                    AndroidAopConfig.verifyLeafExtends = androidAopConfig.verifyLeafExtends
                    AndroidAopConfig.cutInfoJson = androidAopConfig.cutInfoJson
                    AndroidAopConfig.increment = androidAopConfig.increment
                    if (androidAopConfig.cutInfoJson){
                        InitConfig.initCutInfo(project)
                    }
                    if (androidAopConfig.enabled){
                        val javaCompile: AbstractCompile =
                            if (DefaultGroovyMethods.hasProperty(variant, "javaCompileProvider") != null) {
                                //gradle 4.10.1 +
                                variant.javaCompileProvider.get()
                            } else if (DefaultGroovyMethods.hasProperty(variant, "javaCompiler") != null) {
                                variant.javaCompiler as AbstractCompile
                            } else {
                                variant.javaCompile as AbstractCompile
                            }
                        var fullName = ""
                        val names = output.name.split("-");
                        for((index,token) in names.withIndex()){
                            if ("" != token){
                                fullName += (if (index == 0) token else token.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.getDefault()
                                    ) else it.toString()
                                })
                            }
                        }

                        javaCompile.doLast{

                            val localInput = mutableListOf<File>()
                            val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                            if (javaPath.exists()){
                                localInput.add(javaPath)
                            }
                            val kotlinPath = File(project.buildDir.path + "/tmp/kotlin-classes/" + fullName)
                            if (kotlinPath.exists()){
                                localInput.add(kotlinPath)
                            }
                            val jarInput = mutableListOf<File>()
                            val androidConfig = AndroidConfig(project)
                            val list: List<File> = androidConfig.getBootClasspath()
                            val bootJarPath = mutableSetOf<String>()
                            for (file in list) {
                                bootJarPath.add(file.absolutePath)
                            }
                            for (file in localInput) {
                                bootJarPath.add(file.absolutePath)
                            }
                            for (file in javaCompile.classpath) {
                                if (file.absolutePath !in bootJarPath && file.exists()){
                                    jarInput.add(file)
                                }
                            }
                            if (localInput.isNotEmpty()){
                                val output = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                                val task = CompileAndroidAopTask(jarInput,localInput,output,project,isApp)
                                task.taskAction()
                            }
                        }
                    }

                }
            }
        }else{
            if (!isApp) {
                return
            }
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
                AndroidAopConfig.debug = androidAopConfig.debug
                androidAopConfig.includes.forEach {
                    AndroidAopConfig.includes.add("$it.")
                }
                androidAopConfig.excludes.forEach {
                    AndroidAopConfig.excludes.add("$it.")
                }
                AndroidAopConfig.excludes.add(Utils.annotationPackage)
                AndroidAopConfig.excludes.add(Utils.corePackage)
                AndroidAopConfig.verifyLeafExtends = androidAopConfig.verifyLeafExtends
                AndroidAopConfig.cutInfoJson = androidAopConfig.cutInfoJson
                AndroidAopConfig.increment = androidAopConfig.increment
                if (androidAopConfig.cutInfoJson){
                    InitConfig.initCutInfo(project)
                }
                if (androidAopConfig.enabled){
                    val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java)
                    variant.artifacts
                        .forScope(ScopedArtifacts.Scope.ALL)
                        .use(task)
                        .toTransform(
                            ScopedArtifact.CLASSES,
                            AssembleAndroidAopTask::allJars,
                            AssembleAndroidAopTask::allDirectories,
                            AssembleAndroidAopTask::output
                        )
                }
            }
        }
    }
}