package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.flyjingfish.android_aop_plugin.CompileAndroidAopTask
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import java.io.File

object CompilePlugin: BasePlugin() {
    private const val ANDROID_EXTENSION_NAME = "android"

    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val logger = project.logger


        val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)

        val android = project.extensions.findByName(ANDROID_EXTENSION_NAME) as BaseExtension
        val variants = if (isApp or isDynamicLibrary) {
            (android as AppExtension).applicationVariants
        } else {
            (android as LibraryExtension).libraryVariants
        }


        variants.all { variant ->
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
            val variantName = variant.name
            val buildTypeName = variant.buildType.name
            if (!isApp && isDebugMode(buildTypeName,variantName)) {
                logger.warn("Plugin ['android.aop'] 应该被用于 com.android.application 所在 module 下,打正式包时请注意将 androidAop.debugMode 设置为 false")
            }
//            println("CompilePlugin=variant=$variantName,output.name=${variant.buildType.name},isDebug=${isDebugMode(buildTypeName,variantName)}")
            javaCompile.doLast{
                val androidAopConfig : AndroidAopConfig = if (isApp){
                    val config = project.extensions.getByType(AndroidAopConfig::class.java)
                    config.initConfig()
                    config
                }else{
                    var config = InitConfig.optFromJsonString(
                        InitConfig.readAsString(Utils.configJsonFile(project)),
                        AndroidAopConfig::class.java)
                    if (config == null){
                        config = AndroidAopConfig()
                    }
                    config
                }
                if (androidAopConfig.cutInfoJson){
                    InitConfig.initCutInfo(project)
                }
                if (androidAopConfig.enabled && isDebugMode(buildTypeName,variantName)){

                    val localInput = mutableListOf<File>()
                    val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                    if (javaPath.exists()){
                        localInput.add(javaPath)
                    }
                    val kotlinPath = File(project.buildDir.path + "/tmp/kotlin-classes/" + variantName)
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
                            File(Utils.aopCompileTempDir(project,variantName)),
                            File(Utils.invokeJsonFile(project,variantName))
                        )
                        task.taskAction()
                    }
                }


            }
        }
    }
}