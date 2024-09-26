package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.tasks.CompileAndroidAopTask
import com.flyjingfish.android_aop_plugin.tasks.SyncConfigTask
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import java.io.File

class CompilePlugin(private val root:Boolean): BasePlugin() {
    companion object{
        private const val ANDROID_EXTENSION_NAME = "android"
    }

    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        val logger = project.logger


        val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
        val androidObject: Any? = project.extensions.findByName(ANDROID_EXTENSION_NAME)
        if (androidObject == null) {
            // java项目
            project.tasks.withType(JavaCompile::class.java).configureEach { compileTask ->
                compileTask.doLast{
                    if (compileTask !is AbstractCompile){
                        return@doLast
                    }
                    val javaCompile: AbstractCompile = compileTask
                    val compileKotlinTask = project.tasks.named("compileKotlin", KotlinCompile::class.java)
                    if (compileKotlinTask.get() !is KotlinCompile){
                        return@doLast
                    }
                    val compileKotlin = compileKotlinTask.get()
                    val androidAopConfig1 : AndroidAopConfig = if (isApp){
                        val config = project.extensions.getByType(AndroidAopConfig::class.java)
                        config.initConfig()
                        config
                    }else{
                        var config1 = InitConfig.optFromJsonString(
                            InitConfig.readAsString(Utils.configJsonFile(project)),
                            AndroidAopConfig::class.java)
                        if (config1 == null){
                            config1 = AndroidAopConfig()
                        }
                        config1
                    }
                    if (androidAopConfig1.cutInfoJson){
                        InitConfig.initCutInfo(project,false)
                    }
                    val buildTypeName = "release"
                    val variantName = "release"

                    if (androidAopConfig1.enabled && isDebugMode(buildTypeName,variantName)){
                        ClassFileUtils.debugMode = true
                        val hint = "AndroidAOP提示：打正式包时请注意通过设置 androidAop.debugMode 或 androidAop.debugMode.variantOnlyDebug 关闭debug模式"
                        logger.error(hint)
                        val localInput = mutableListOf<File>()
                        val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                        if (javaPath.exists()){
                            localInput.add(javaPath)
                        }
                        val task = compileKotlin
                        val cacheDir = try {
                            task.destinationDirectory.get().asFile
                        } catch (e: Exception) {
                            null
                        }
                        val kotlinPath = cacheDir ?: File(project.buildDir.path + "/classes/kotlin/main".adapterOSPath())

                        if (kotlinPath.exists()){
                            localInput.add(kotlinPath)
                        }
                        val jarInput = mutableListOf<File>()
                        val bootJarPath = mutableSetOf<String>()
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
                            ClassFileUtils.reflectInvokeMethod = isReflectInvokeMethod(buildTypeName,variantName)
                            val output = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                            val task = CompileAndroidAopTask(jarInput,localInput,output,project,isApp,
                                File(Utils.aopCompileTempDir(project,variantName)),
                                File(Utils.invokeJsonFile(project,variantName)),
                                variantName,false
                            )
                            task.taskAction()
                        }
                    }


                }
            }
            return
        }


        val hasConfig = project.extensions.findByName("androidAopConfig") != null
        val syncConfig = !root && hasConfig && isApp
        if (syncConfig){
            val taskName = "${project.name}AndroidAopConfigSyncTask"
            project.tasks.register(taskName, SyncConfigTask::class.java)
            project.afterEvaluate {
                project.tasks.findByName("preBuild")?.finalizedBy(taskName)
            }
        }


        val kotlinCompileFilePathMap = mutableMapOf<String, KotlinCompileTool>()
        val android = androidObject as BaseExtension
        val variants = if (isApp or isDynamicLibrary) {
            (android as AppExtension).applicationVariants
        } else {
            (android as LibraryExtension).libraryVariants
        }
        variants.all { variant ->
            if (syncConfig){
                AndroidAopConfig.syncConfig(project)
            }
            try {
                project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                    kotlinCompileFilePathMap[task.name] = task
                }
            } catch (_: Exception) {
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
            if (!isIncremental() && javaCompile is JavaCompile && isDebugMode(buildTypeName,variantName)){
                javaCompile.options.isIncremental = false
            }
//            println("CompilePlugin=variant=$variantName,output.name=${variant.buildType.name},isDebug=${isDebugMode(buildTypeName,variantName)}")
            if (isApp && isIncremental()){
                javaCompile.doFirst{
                    val enabled = try {
                        val firstConfig = project.extensions.getByType(AndroidAopConfig::class.java)
                        firstConfig.enabled
                    } catch (e: Exception) {
                        true
                    }
                    if (enabled && isDebugMode(buildTypeName,variantName)){
                        WovenIntoCode.deleteOtherCompileClass(project, variantName)
                    }
                }
            }
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
                    InitConfig.initCutInfo(project,false)
                }
                if (androidAopConfig.enabled && isDebugMode(buildTypeName,variantName)){
                    ClassFileUtils.debugMode = true
                    val hint = "AndroidAOP提示：打正式包时请注意通过设置 androidAop.debugMode 或 androidAop.debugMode.variantOnlyDebug 关闭debug模式"
                    if (buildTypeName == "release"){
                        logger.error(hint)
                    }else{
                        logger.warn(hint)
                    }
                    val localInput = mutableListOf<File>()
                    val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                    if (javaPath.exists()){
                        localInput.add(javaPath)
                    }
                    val task = kotlinCompileFilePathMap["compile${variantName.capitalized()}Kotlin"]
                    val cacheDir = try {
                        task?.destinationDirectory?.get()?.asFile
                    } catch (e: Exception) {
                        null
                    }
                    val kotlinPath = cacheDir ?: File(project.buildDir.path + "/tmp/kotlin-classes/".adapterOSPath() + variantName)

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
                        ClassFileUtils.reflectInvokeMethod = isReflectInvokeMethod(buildTypeName,variantName)
                        val output = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                        val task = CompileAndroidAopTask(jarInput,localInput,output,project,isApp,
                            File(Utils.aopCompileTempDir(project,variantName)),
                            File(Utils.invokeJsonFile(project,variantName)),
                            variantName
                        )
                        task.taskAction()
                    }
                }


            }
        }
    }
}