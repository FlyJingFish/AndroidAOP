package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.scanner_visitor.AnnotationMethodScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.AnnotationScanner
import com.flyjingfish.android_aop_plugin.utils.Utils.MethodAnnoUtils
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.WovenInfo
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipException
import kotlin.system.measureTimeMillis

abstract class AssembleAndroidAopTask : DefaultTask() {


    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private var jarOutput: JarOutputStream? = null
    private companion object {
        private const val END_NAME = "\$\$AndroidAopClass.class"
        private const val _CLASS = ".class"
    }
    private lateinit var temporaryDir: File
    private lateinit var buildConfigCacheFile: File
    private val gson: Gson = GsonBuilder().create()
    fun <T> optFromJsonString(jsonString: String, clazz: Class<T>): T? {
        try {
            return gson.fromJson(jsonString, clazz)
        } catch (e: Throwable) {
            logger.warn("optFromJsonString(${jsonString}, $clazz", e)
        }
        return null
    }

    fun optToJsonString(any: Any): String {
        try {
            return gson.toJson(any)
        } catch (throwable: Throwable) {
            logger.warn("optToJsonString(${any}", throwable)
        }
        return ""
    }

    fun loadBuildConfig(buildConfigCacheFile: File): WovenInfo {
        return if (buildConfigCacheFile.exists()) {
            val jsonString = readAsString(buildConfigCacheFile.absolutePath)
            optFromJsonString(jsonString, WovenInfo::class.java) ?: WovenInfo()
        } else {
            WovenInfo()
        }
    }
    fun saveBuildConfig() {
        val wovenInfo = WovenInfo()
        wovenInfo.aopMatchCuts = WovenInfoUtils.aopMatchCuts
        wovenInfo.aopMethodCuts = WovenInfoUtils.aopMethodCuts
        wovenInfo.classPaths = WovenInfoUtils.classPaths
        saveFile(buildConfigCacheFile, optToJsonString(wovenInfo))
    }
    fun saveFile(file: File,data:String) {
        val fos = FileOutputStream(file.absolutePath)
        try {
            fos.write(data.toByteArray())
        } catch (e:IOException) {
            e.printStackTrace();
        }finally {
            fos.close()
        }
    }
    public fun readAsString(path :String) :String {
        return try {
            val content = String(Files.readAllBytes(Paths.get(path)));
            content
        }catch (exception:Exception){
            ""
        }
    }
    @TaskAction
    fun taskAction() {
        temporaryDir = File(project.buildDir.absolutePath+"/tmp")
        buildConfigCacheFile = File(temporaryDir, "buildAndroidAopConfigCache.json")
//        val wovenInfo = loadBuildConfig(buildConfigCacheFile)
//        if(wovenInfo.aopMatchCuts != null){
//            WovenInfoUtils.aopMatchCuts = wovenInfo.aopMatchCuts!!
//        }
//        if(wovenInfo.aopMethodCuts != null){
//            WovenInfoUtils.aopMethodCuts = wovenInfo.aopMethodCuts!!
//        }
//        if(wovenInfo.classPaths != null){
//            WovenInfoUtils.classPaths = wovenInfo.classPaths!!
//        }

//        logger.error("buildConfigCacheFile = ${buildConfigCacheFile.absolutePath}")
//        logger.error("buildConfig = $wovenInfo")

        jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        jarOutput!!.close()
        logger.info("Scan finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() {
        val androidConfig : AndroidConfig = AndroidConfig(project)
        val list: List<File> = androidConfig.getBootClasspath()
        logger.info("Scan to classPath [${list}]")
        for (file in list) {
            WovenInfoUtils.addClassPath(file.absolutePath)
        }
        //第一遍找配置文件
        allDirectories.get().forEach { directory ->
            WovenInfoUtils.addClassPath(directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith(END_NAME)) {
                        FileInputStream(file).use { inputs ->
                            val classReader = ClassReader(inputs.readAllBytes())
                            classReader.accept(
                                AnnotationScanner(
                                    logger
                                ), ClassReader.EXPAND_FRAMES)
                        }
                    }

                }
            }

        }

        allJars.get().forEach { file ->
            WovenInfoUtils.addClassPath(file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                        continue
                    }

                    if (entryName.endsWith(END_NAME)) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val classReader = ClassReader(inputs.readAllBytes())
                            classReader.accept(
                                AnnotationScanner(
                                    logger
                                ), ClassReader.EXPAND_FRAMES)
                        }
                    }
                } catch (e: Exception) {
                    if (!(e is ZipException && e.message?.startsWith("duplicate entry:") == true)) {
                        logger.warn("Merge jar error entry:[${jarEntry.name}], error message:$e")
                    }
                }
            }
            jarFile.close()
        }

        saveBuildConfig()
//        ClassPoolUtils.initClassPool()
//        logger.error("第二遍找到要注入代码的类---------")
        //第二遍找配置有注解的类和方法
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith(_CLASS)) {

                        FileInputStream(file).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()){
                                try {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(AnnotationMethodScanner(
                                        logger, bytes
                                    ) {
                                        val record = ClassMethodRecord(file.absolutePath, it)
                                        WovenInfoUtils.addClassMethodRecords(record)
//                                        logger.error("Scanned method:[${file.absolutePath}][${it}]")
                                    }, ClassReader.EXPAND_FRAMES)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }

                }
            }
        }
//        logger.info("Scan to project [${project.project.}]")
        allJars.get().forEach { file ->
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                        continue
                    }
                    if (entryName.endsWith(_CLASS)) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()){
                                try {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(AnnotationMethodScanner(
                                        logger, bytes
                                    ) {
                                        val record = ClassMethodRecord(entryName, it)
                                        WovenInfoUtils.addClassMethodRecords(record)
//                                        logger.error("Scanned method:[${entryName}][${it}]")
                                    }, ClassReader.EXPAND_FRAMES)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!(e is ZipException && e.message?.startsWith("duplicate entry:") == true)) {
                        logger.error("Merge jar error entry:[${jarEntry.name}], error message:$e")
                    }
                }
            }
            jarFile.close()
        }
//        logger.error("第三遍---------")
        allDirectories.get().forEach { directory ->
//            logger.error("Scan to directory1 [${directory.asFile.absolutePath}]")
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
//                    logger.error("Scan to directory2 [${file.absolutePath}]")
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path


                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                    if (methodsRecord != null){
//                        logger.error("Scan to methodsRecord [${methodsRecord}]")
                        FileInputStream(file).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord)
                            jarOutput!!.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                            ByteArrayInputStream(byteArray).use {
                                it.copyTo(jarOutput!!)
                            }
                            jarOutput!!.closeEntry()
                        }
                    }else{
                        jarOutput!!.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput!!)
                        }
                        jarOutput!!.closeEntry()
                    }
                }
            }
        }
//        logger.info("Scan to project [${project.project.}]")
        allJars.get().forEach { file ->
            logger.info("Scan to jar [${file.asFile.absolutePath}]")
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    logger.info("Scan to jar---- [${jarEntry}]")
//                    if (jarEntry.isDirectory || entryName.isEmpty() || !entryName.endsWith(_CLASS) || entryName.startsWith("META-INF/")) {
//                        continue
//                    }
                    if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/")) {
                        continue
                    }

                    logger.info("Scan to jar ==== [${jarEntry}][${entryName}]")


                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)

                    if (methodsRecord != null){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord)
                            jarOutput!!.putNextEntry(JarEntry(entryName))
                            ByteArrayInputStream(byteArray).use {
                                it.copyTo(jarOutput!!)
                            }
                            jarOutput!!.closeEntry()
                        }
                    }else if (Utils.dotToSlash(MethodAnnoUtils) + _CLASS == entryName) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val originInject = inputs.readAllBytes()
                            val resultByteArray = RegisterMapWovenInfoCode().execute(ByteArrayInputStream(originInject))
                            jarOutput!!.putNextEntry(JarEntry(entryName))
                            ByteArrayInputStream(resultByteArray).use {
                                it.copyTo(jarOutput!!)
                            }
                            jarOutput!!.closeEntry()
                        }
                    } else{
                        jarOutput!!.putNextEntry(JarEntry(entryName))
                        jarFile.getInputStream(jarEntry).use {
                            it.copyTo(jarOutput!!)
                        }
                        jarOutput!!.closeEntry()
                    }


                } catch (e: Exception) {
                    throw RuntimeException("Merge jar error entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                }
            }
            jarFile.close()
        }
    }
}