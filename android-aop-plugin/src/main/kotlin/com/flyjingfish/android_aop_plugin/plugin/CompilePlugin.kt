package com.flyjingfish.android_aop_plugin.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.tasks.CompileAndroidAopTask
import com.flyjingfish.android_aop_plugin.tasks.DebugModeFileTask
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.RuntimeProject
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import com.flyjingfish.android_aop_plugin.utils.getRelativePath
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool
import java.io.File

class CompilePlugin(private val fromRootSet:Boolean): BasePlugin() {
    companion object{
        const val ANDROID_EXTENSION_NAME = "android"
        private const val DEBUG_MODE_FILE_TASK_NAME = "debugModeFile"
        private val kotlinCompileFilePathMap = mutableMapOf<String, File>()
    }


    override fun apply(project: Project) {
        super.apply(project)
        if (!fromRootSet && project.rootProject == project){
            project.rootProject.gradle.taskGraph.addTaskExecutionGraphListener(object :
                TaskExecutionGraphListener {
                override fun graphPopulated(it: TaskExecutionGraph) {
                    try {
                        for (task in it.allTasks) {
                            if (task is KotlinCompileTool){
                                val destinationDirectory = task.destinationDirectory.get().asFile
                                val key = task.project.buildDir.absolutePath+"@"+task.name
                                val oldDirectory = kotlinCompileFilePathMap[key]
                                if (oldDirectory == null || oldDirectory.absolutePath != destinationDirectory.absolutePath){
                                    kotlinCompileFilePathMap[key] = destinationDirectory
                                }
                            }
                        }
                    } catch (_: Throwable) {
                    }
                    project.rootProject.gradle.taskGraph.removeTaskExecutionGraphListener(this)
                }

            })
        }
        val runtimeProject = RuntimeProject.getAfterEvaluate(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)


        val isDynamicLibrary = project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)
        val androidObject: Any? = project.extensions.findByName(ANDROID_EXTENSION_NAME)
        if (androidObject == null) {
            if (project.rootProject != project && !fromRootSet){
                project.afterEvaluate {
                    val isAndroidProject: Boolean = project.extensions.findByName(ANDROID_EXTENSION_NAME) != null
                    if (isAndroidProject){
                        if (project.plugins.hasPlugin(LibraryPlugin::class.java)){
                            throw RuntimeException("In the module of ${project.name}, [id 'android.aop'] must be written below [id 'com.android.library']")
                        }else if (project.plugins.hasPlugin(DynamicFeaturePlugin::class.java)){
                            throw RuntimeException("In the module of ${project.name}, [id 'android.aop'] must be written below [id 'com.android.dynamic-feature']")
                        }
                    }
                    val isApp2 = project.plugins.hasPlugin(AppPlugin::class.java)
                    if (!isApp2 && isAndroidProject){
                        throw RuntimeException("In the module of ${project.name}, [id 'android.aop'] must be written below [id 'com.android.library']")
                    }
                }
            }
            if (project.rootProject == project || fromRootSet){
                return
            }
            val buildTypeName = "release"
            val variantName = "release"
            if (hasBuildConfig()){
                try {
                    val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
                    val path = Utils.aopDebugModeJavaDir4Java()
                    val debugModeDir = File("${project.buildDir.absolutePath}$path")
                    var packageName :String ?=null
                    for (srcDir in javaPluginExtension.sourceSets.getByName("main").java.srcDirs) {
                        if (srcDir.absolutePath != debugModeDir.absolutePath){
                            if (srcDir.exists() && packageName == null){
                                //说明这个才是真正的源码所在路径
                                val packageFile = getPackageNameFile(srcDir,0)
                                val relativePath = packageFile.getRelativePath(srcDir).replace(File.separator,".").replace("/",".")
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
                            it.pluginConfig = pluginConfig
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
            var androidObject1: Any? = null
            project.afterEvaluate {
                androidObject1 = project.extensions.findByName(ANDROID_EXTENSION_NAME)
            }
            // java项目
            val kotlinDefaultPath = File(project.buildDir.path + "/classes/kotlin/main".adapterOSPath())
            project.tasks.withType(JavaCompile::class.java).configureEach { compileTask ->
                if (isDebugMode() && compileTask is AbstractCompile){
                    forceJavaRecompileOnKotlinChange(project, compileTask, "")
                }

                compileTask.doLast{
                    if (androidObject1 != null) {
                        return@doLast
                    }
                    if (compileTask !is AbstractCompile){
                        return@doLast
                    }
                    val javaCompile: AbstractCompile = compileTask
                    doAopTask(runtimeProject, isApp, variantName, buildTypeName, javaCompile, kotlinDefaultPath,false)
                }
            }
            return
        }


        val hasConfig = project.extensions.findByName("androidAopConfig") != null
        val syncConfig = !fromRootSet && hasConfig && isApp
        if (syncConfig){
            project.afterEvaluate {
                AndroidAopConfig.syncConfig(project)
                if (AndroidAopConfig.cutInfoJson){
                    InitConfig.initCutInfo(project,false)
                }
            }
        }


        val android = androidObject as BaseExtension
        val variants = if (isApp or isDynamicLibrary) {
            (android as AppExtension).applicationVariants
        } else {
            (android as LibraryExtension).libraryVariants
        }
        variants.all { variant ->
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
                    val enabled =  AndroidAopConfig.enabled
                    if (enabled && isDebugMode(buildTypeName,variantName)){
                        WovenIntoCode.deleteOtherCompileClass(runtimeProject, variantName)
                    }
                }
            }
            if (isDebugMode(buildTypeName,variantName)){
                forceJavaRecompileOnKotlinChange(project, javaCompile, variantName)
            }
            val kotlinBuildPath = File(project.buildDir.path + "/tmp/kotlin-classes/".adapterOSPath() + variantName)
            javaCompile.doLast{
                doAopTask(runtimeProject,isApp, variantName, buildTypeName, javaCompile, kotlinBuildPath)
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
                        it.pluginConfig = pluginConfig
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
                    project.tasks.findByName("pre${variantNameCapitalized}Build")?.dependsOn("$DEBUG_MODE_FILE_TASK_NAME$variantNameCapitalized")
                }
            }
        }
    }

    private fun forceJavaRecompileOnKotlinChange(project: Project, javaCompile: AbstractCompile, variantName :String){
        try {
            javaCompile.dependsOn("compile${variantName.capitalized()}Kotlin")
            javaCompile.inputs.files(project.tasks.named("compile${variantName.capitalized()}Kotlin").map { it.outputs.files })
                .withPropertyName("kotlinClasses")
                .withPathSensitivity(PathSensitivity.RELATIVE)
        } catch (_: Exception) {
        }

        if (hasBuildConfig()){
            val generatedDir = if (variantName.isEmpty()){
                project.file("build"+Utils.aopDebugModeJavaDir4JavaNoAdapter())
            }else{
                project.file("build"+Utils.aopDebugModeJavaDirNoAdapter(variantName))
            }
            javaCompile.source(generatedDir)
        }
    }

    private fun getPackageName(srcDirs :Set<File>,debugModeDir:File):String?{
        for (srcDir in srcDirs) {
            if (srcDir.absolutePath != debugModeDir.absolutePath){
                if (srcDir.exists()){
                    //说明这个才是真正的源码所在路径
                    val packageFile = getPackageNameFile(srcDir,0)
                    val relativePath = packageFile.getRelativePath(srcDir).replace(File.separator,".").replace("/",".")
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

    private fun doAopTask(project: RuntimeProject,isApp:Boolean, variantName: String, buildTypeName: String,
                          javaCompile:AbstractCompile, kotlinDefaultPath: File, isAndroidModule : Boolean = true){
        val cacheDir = if (isAndroidModule) {
            kotlinCompileFilePathMap[project.buildDir.absolutePath+"@"+"compile${variantName.capitalized()}Kotlin"]
        }else{
            kotlinCompileFilePathMap[project.buildDir.absolutePath+"@"+"compileKotlin"]
        }
        val kotlinPath = cacheDir ?: kotlinDefaultPath
        val logger = javaCompile.logger

        val debugMode = if (isAndroidModule){
            isDebugMode(buildTypeName,variantName)
        }else{
            isDebugMode()
        }
        if (AndroidAopConfig.enabled && debugMode){
            ClassFileUtils.debugMode = true
            val hint = "AndroidAOP Tip: You are using debugMode mode"
            if (buildTypeName == "release"){
                logger.error(hint)
            }else{
                logger.warn(hint)
            }
            val localInput = mutableSetOf<String>()
            val javaPath = File(javaCompile.destinationDirectory.asFile.orNull.toString())
            if (javaPath.exists()){
                localInput.add(javaPath.absolutePath)
            }

            if (kotlinPath.exists()){
                localInput.add(kotlinPath.absolutePath)
            }
            val jarInput = mutableSetOf<String>()
            val bootJarPath = mutableSetOf<String>()
            if (isAndroidModule){
                val list: List<File> = project.androidConfig.getBootClasspath()
                for (file in list) {
                    bootJarPath.add(file.absolutePath)
                }
            }
            for (file in localInput) {
                bootJarPath.add(file)
            }
            for (file in javaCompile.classpath) {
                if (file.absolutePath !in bootJarPath && file.exists()){
                    if (file.isDirectory){
                        localInput.add(file.absolutePath)
                    }else{
                        jarInput.add(file.absolutePath)
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
                val jarInputFiles: List<File> = jarInput.map(::File)
                val localInputFiles: List<File> = localInput.map(::File)
                val task = CompileAndroidAopTask(jarInputFiles,localInputFiles,output,project,isApp,
                    File(Utils.aopCompileTempDir(project,variantName)),
                    File(Utils.invokeJsonFile(project,variantName)),
                    variantName,javaCompile,isAndroidModule
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