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
import com.flyjingfish.android_aop_plugin.config.RootBooleanConfig
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
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

        val reflectInvokeMethodStr = project.properties[RootBooleanConfig.REFLECT_INVOKE_METHOD.propertyName]?:"false"
        val debugModeStr = project.properties[RootBooleanConfig.DEBUG_MODE.propertyName]?:"false"
        val debugMode = debugModeStr == "true"
        val reflectInvokeMethod = reflectInvokeMethodStr == "true"
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
                    if (isApp){
                        val androidAopConfig = project.extensions.getByType(AndroidAopConfig::class.java)
                        androidAopConfig.initConfig()

                        for (childProject in project.rootProject.childProjects) {
                            if (project != childProject.value){
                                val configFile = File(Utils.configJsonFile(childProject.value))
                                InitConfig.exportConfigJson(configFile,androidAopConfig)
                            }
                        }
                    }
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
                        val androidAopConfig : AndroidAopConfig = if (isApp){
                            val config = project.extensions.getByType(AndroidAopConfig::class.java)
                            config.initConfig()
                            config
                        }else{
                            var config = InitConfig.optFromJsonString(InitConfig.readAsString(Utils.configJsonFile(project)),AndroidAopConfig::class.java)
                            if (config == null){
                                config = AndroidAopConfig()
                            }
                            config
                        }
                        if (androidAopConfig.cutInfoJson){
                            InitConfig.initCutInfo(project)
                        }
                        if (androidAopConfig.enabled){

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
                                    if (file.isDirectory){
                                        localInput.add(file)
                                    }else{
                                        jarInput.add(file)
                                    }
                                }
                            }
                            if (localInput.isNotEmpty()){
                                ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
                                val output = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                                val task = CompileAndroidAopTask(jarInput,localInput,output,project,isApp,
                                    File(Utils.aopCompileTempDir(project,fullName)),
                                    File(Utils.invokeJsonFile(project,fullName))
                                )
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
                androidAopConfig.initConfig()
                if (androidAopConfig.cutInfoJson){
                    InitConfig.initCutInfo(project)
                }
                if (androidAopConfig.enabled){
                    ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
                    val task = project.tasks.register("${variant.name}AssembleAndroidAopTask", AssembleAndroidAopTask::class.java){
                        it.variant = variant.name
                    }
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