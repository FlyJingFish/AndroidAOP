package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.tasks.CompileAndroidAopTask
import com.flyjingfish.android_aop_plugin.tasks.DebugModeFileTask
import com.flyjingfish.android_aop_plugin.tasks.SyncConfigTask
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import com.flyjingfish.android_aop_plugin.utils.getRelativePath
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import java.io.File

class CompilePlugin(private val root:Boolean): BasePlugin() {
    companion object{
        private const val ANDROID_EXTENSION_NAME = "android"
        private const val DEBUG_MODE_FILE_TASK_NAME = "debugModeFile"
    }

    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)


        val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
        val androidObject: Any? = project.extensions.findByName(ANDROID_EXTENSION_NAME)
        if (androidObject == null) {
            if (project.rootProject == project || root){
                return
            }
            val buildTypeName = "release"
            val variantName = "release"
            if (hasBuildConfig()){
                try {
                    val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
                    val path = Utils.aopDebugModeJavaDir4Java()
                    val debugModeDir = File("${project.buildDir.absolutePath}$path")
                    if (!debugModeDir.exists()){
                        debugModeDir.mkdirs()
                    }
                    // 设置新的 Java 源代码路径
                    javaPluginExtension.sourceSets.getByName("main").java.srcDirs("build$path")
                    var packageName :String ?=null
                    for (srcDir in javaPluginExtension.sourceSets.getByName("main").java.srcDirs) {
                        if (srcDir.absolutePath != debugModeDir.absolutePath){
                            if (srcDir.exists() && packageName == null){
                                //说明这个才是真正的源码所在路径
                                val packageFile = getPackageNameFile(srcDir,0)
                                val relativePath = packageFile.getRelativePath(srcDir).replace(File.separator,".")
                                packageName = if (relativePath.endsWith(".")){
                                    relativePath.substring(0,relativePath.length-1)
                                }else{
                                    relativePath
                                }
                            }
                        }
                    }
                    if (packageName != null){
                        if (packageName.isEmpty()){
                            packageName = project.name.replace("-","_")
                        }
                        project.tasks.register(DEBUG_MODE_FILE_TASK_NAME, DebugModeFileTask::class.java){
                            it.debugModeDir = debugModeDir.absolutePath
                            it.packageName = packageName
                            it.variantName = variantName
                            it.buildTypeName = buildTypeName
                            it.isAndroidModule = false
                        }
                        project.afterEvaluate {
                            project.tasks.findByName("compileJava")?.dependsOn(DEBUG_MODE_FILE_TASK_NAME)
                        }
                    }
                } catch (_: Throwable) {
                }
            }


            // java项目
            project.tasks.withType(JavaCompile::class.java).configureEach { compileTask ->
                compileTask.doLast{
                    val androidObject1: Any? = project.extensions.findByName(ANDROID_EXTENSION_NAME)
                    if (androidObject1 != null) {
                        return@doLast
                    }
                    if (compileTask !is AbstractCompile){
                        return@doLast
                    }
                    val javaCompile: AbstractCompile = compileTask
                    val compileKotlinTask = project.tasks.named("compileKotlin", KotlinCompile::class.java)
                    if (compileKotlinTask.get() !is KotlinCompile){
                        return@doLast
                    }
                    val compileKotlin = compileKotlinTask.get()



                    val cacheDir = try {
                        compileKotlin.destinationDirectory.get().asFile
                    } catch (e: Throwable) {
                        null
                    }
                    val kotlinPath = cacheDir ?: File(project.buildDir.path + "/classes/kotlin/main".adapterOSPath())
                    doAopTask(project, isApp, variantName, buildTypeName, javaCompile, kotlinPath,false)
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
        try {
            project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
                kotlinCompileFilePathMap[task.name] = task
            }
        } catch (_: Throwable) {
        }
        variants.all { variant ->
            if (syncConfig){
                AndroidAopConfig.syncConfig(project)
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
            if (isApp && isIncremental()){
                javaCompile.doFirst{
                    val enabled = try {
                        val firstConfig = project.extensions.getByType(AndroidAopConfig::class.java)
                        firstConfig.enabled
                    } catch (e: Throwable) {
                        true
                    }
                    if (enabled && isDebugMode(buildTypeName,variantName)){
                        WovenIntoCode.deleteOtherCompileClass(project, variantName)
                    }
                }
            }
            javaCompile.doLast{

                val task = try {
                    kotlinCompileFilePathMap["compile${variantName.capitalized()}Kotlin"]
                } catch (_: Throwable) {
                    null
                }
                val cacheDir = try {
                    task?.destinationDirectory?.get()?.asFile
                } catch (e: Throwable) {
                    null
                }
                val kotlinPath = cacheDir ?: File(project.buildDir.path + "/tmp/kotlin-classes/".adapterOSPath() + variantName)
                doAopTask(project, isApp, variantName, buildTypeName, javaCompile, kotlinPath)
            }
        }
        if (hasBuildConfig()){
            val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
            val variantList: ArrayList<Variant> = ArrayList()
            androidComponents.onVariants { variant ->
                variantList.add(variant)
                val variantName = variant.name
                val path = Utils.aopDebugModeJavaDir(variantName)
                val debugModeDir = File("${project.buildDir.absolutePath}$path")
                variant.sources.java?.let { java ->
                    if (!debugModeDir.exists()){
                        debugModeDir.mkdirs()
                    }
                    java.addStaticSourceDirectory("build$path")
                }
                val variantNameCapitalized = variantName.capitalized()
                var packageName = if (android.namespace == null || android.namespace == "null"){
                    android.defaultConfig.applicationId.toString()
                }else{
                    android.namespace.toString()
                }

                if (packageName == "null"){
                    for (sourceSet in android.sourceSets) {
                        if (sourceSet.name == "main"){
                            val pkName = getPackageName(sourceSet.java.srcDirs,debugModeDir)
                            if (pkName != null){
                                packageName = pkName
                                break
                            }
                        }
                    }
                }

                if (packageName == "null"){
                    for (sourceSet in android.sourceSets) {
                        if (sourceSet.name == "release"){
                            val pkName = getPackageName(sourceSet.java.srcDirs,debugModeDir)
                            if (pkName != null){
                                packageName = pkName
                                break
                            }
                        }
                    }
                }

                if (packageName == "null"){
                    for (sourceSet in android.sourceSets) {
                        if (sourceSet.name == "debug"){
                            val pkName = getPackageName(sourceSet.java.srcDirs,debugModeDir)
                            if (pkName != null){
                                packageName = pkName
                                break
                            }
                        }
                    }
                }
                if (packageName.isEmpty()){
                    packageName = project.name.replace("-","_")
                }
                val buildTypeName: String? = variant.buildType
                project
                    .tasks
                    .register("$DEBUG_MODE_FILE_TASK_NAME$variantNameCapitalized", DebugModeFileTask::class.java){
                        it.debugModeDir = debugModeDir.absolutePath
                        it.packageName = packageName
                        it.variantName = variantName
                        it.buildTypeName = buildTypeName
                        it.isAndroidModule = true
                    }
            }
            project.afterEvaluate {
                for (variant in variantList) {
                    val variantName = variant.name
                    val variantNameCapitalized = variantName.capitalized()
                    project.tasks.findByName("pre${variantNameCapitalized}Build")?.finalizedBy("$DEBUG_MODE_FILE_TASK_NAME$variantNameCapitalized")
                }
            }
        }
    }

    private fun getPackageName(srcDirs :Set<File>,debugModeDir:File):String?{
        for (srcDir in srcDirs) {
            if (srcDir.absolutePath != debugModeDir.absolutePath){
                if (srcDir.exists()){
                    //说明这个才是真正的源码所在路径
                    val packageFile = getPackageNameFile(srcDir,0)
                    val relativePath = packageFile.getRelativePath(srcDir).replace("/",".")
                    return if (relativePath.endsWith(".")){
                        relativePath.substring(0,relativePath.length-1)
                    }else{
                        relativePath
                    }

                }
            }
        }

        return null
    }

    private fun doAopTask(project: Project, isApp:Boolean, variantName: String, buildTypeName: String,
                          javaCompile:AbstractCompile, kotlinPath: File, isAndroidModule : Boolean = true){
        val logger = project.logger
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
        val debugMode = if (isAndroidModule){
            isDebugMode(buildTypeName,variantName)
        }else{
            isDebugMode()
        }
        if (androidAopConfig.enabled && debugMode){
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

            if (kotlinPath.exists()){
                localInput.add(kotlinPath)
            }
            val jarInput = mutableListOf<File>()
            val bootJarPath = mutableSetOf<String>()
            if (isAndroidModule){
                val androidConfig = AndroidConfig(project)
                val list: List<File> = androidConfig.getBootClasspath()
                for (file in list) {
                    bootJarPath.add(file.absolutePath)
                }
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
                val reflectInvokeMethod = if (isAndroidModule){
                    isReflectInvokeMethod(buildTypeName,variantName)
                }else{
                    isReflectInvokeMethod()
                }
                ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
                ClassFileUtils.reflectInvokeMethodStatic = isReflectInvokeMethodStatic()
                val output = File(javaCompile.destinationDirectory.asFile.orNull.toString())
                val task = CompileAndroidAopTask(jarInput,localInput,output,project,isApp,
                    File(Utils.aopCompileTempDir(project,variantName)),
                    File(Utils.invokeJsonFile(project,variantName)),
                    variantName,isAndroidModule
                )
                task.taskAction()
            }
        }
    }

    private fun getPackageNameFile(file: File,deep:Int): File {
        if (!file.isDirectory){
            return file.parentFile
        }
        val files = file.listFiles()
        if (files != null){
            return if (files.size == 1){
                if (files[0].isDirectory){
                    getPackageNameFile(files[0],deep+1)
                }else{
                    files[0].parentFile
                }

            }else{
                if (files.isNotEmpty()){
                    getPackageNameFile(files[0],deep+1)
                }else{
                    file
                }
            }
        }else{
            return file
        }
    }
}