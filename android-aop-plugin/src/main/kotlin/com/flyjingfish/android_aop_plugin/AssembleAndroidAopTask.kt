package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.AnnotationMethodScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.AnnotationScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.ClassSuperScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    private lateinit var jarOutput: JarOutputStream
    private companion object {
        private const val END_NAME = "\$\$AndroidAopClass.class"
        private const val _CLASS = ".class"
    }

    @TaskAction
    fun taskAction() {
        jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        jarOutput.close()
        printLog("Woven info code finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() {
        loadJoinPointConfig()
        searchJoinPointLocation()
        wovenIntoCode()
    }

    private fun loadJoinPointConfig(){
        val androidConfig = AndroidConfig(project)
        val list: List<File> = androidConfig.getBootClasspath()
        printLog("Scan to classPath [${list}]")
        WovenInfoUtils.clear()
        for (file in list) {
            WovenInfoUtils.addClassPath(file.absolutePath)
            try {
                val jarFile = JarFile(file)
                val enumeration = jarFile.entries()
//                logger.error("jarFile=$jarFile,enumeration=$enumeration")
                while (enumeration.hasMoreElements()) {
                    val jarEntry = enumeration.nextElement()
                    try {
                        val entryName = jarEntry.name
//                        logger.error("entryName="+entryName)
                        if (entryName.endsWith(_CLASS)){
                            WovenInfoUtils.addClassName(entryName)
                        }
                    } catch (_: Exception) {

                    }
                }
                jarFile.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //第一遍找配置文件
        allDirectories.get().forEach { directory ->
//            printLog("directory.asFile.absolutePath = ${directory.asFile.absolutePath}")
            val directoryPath = directory.asFile.absolutePath
            WovenInfoUtils.addClassPath(directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
//                    logger.error("file.name="+file.absolutePath)
                    if (file.name.endsWith(END_NAME)) {
                        FileInputStream(file).use { inputs ->
                            val classReader = ClassReader(inputs.readAllBytes())
                            classReader.accept(
                                AnnotationScanner(
                                    logger
                                ), ClassReader.EXPAND_FRAMES)
                        }
                    }else if (file.absolutePath.endsWith(_CLASS)){
                        val className = file.absolutePath.replace("$directoryPath/","")
                        WovenInfoUtils.addClassName(className)
                        if (AndroidAopConfig.verifyLeafExtends && !className.startsWith("kotlinx/") && !className.startsWith("kotlin/")){
                            FileInputStream(file).use { inputs ->
                                val classReader = ClassReader(inputs.readAllBytes())
                                classReader.accept(
                                    ClassSuperScanner(), ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                            }
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
                    if (entryName.endsWith(_CLASS)){
                        WovenInfoUtils.addClassName(entryName)
                    }
//                    logger.error("entryName="+entryName)
                    if (entryName.endsWith(END_NAME)) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val classReader = ClassReader(inputs.readAllBytes())
                            classReader.accept(
                                AnnotationScanner(
                                    logger
                                ), ClassReader.EXPAND_FRAMES)
                        }
                    }else if (entryName.endsWith(_CLASS)){
                        if (AndroidAopConfig.verifyLeafExtends && !entryName.startsWith("kotlinx/") && !entryName.startsWith("kotlin/")){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val classReader = ClassReader(inputs.readAllBytes())
                                classReader.accept(
                                    ClassSuperScanner(), ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                            }
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
//        logger.error(""+WovenInfoUtils.aopMatchCuts)
//        InitConfig.saveBuildConfig()
        ClassPoolUtils.initClassPool()
    }

    private fun searchJoinPointLocation(){
        val includes = AndroidAopConfig.includes
        val excludes = AndroidAopConfig.excludes
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val isClassFile = file.name.endsWith(_CLASS)
                    val tranEntryName = file.absolutePath.replace("/", ".")
                        .replace("\\", ".")
//                    printLog("tranEntryName="+tranEntryName)
//                    if (tranEntryName.contains("com.flyjingfish")){
//                        printLog("isIncludeFilterMatched="+Utils.isIncludeFilterMatched(tranEntryName, includes) )
//                        printLog("isExcludeFilterMatched="+Utils.isExcludeFilterMatched(tranEntryName, excludes) )
//                    }
                    if (isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)) {
                        FileInputStream(file).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()){
                                try {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(AnnotationMethodScanner(
                                        logger
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
                    val isClassFile = entryName.endsWith(_CLASS)
                    val tranEntryName = entryName.replace("/", ".")
                        .replace("\\", ".")
//                    printLog("tranEntryName="+tranEntryName)
//                    if (tranEntryName.contains("com.flyjingfish")){
//                        printLog("isIncludeFilterMatched="+Utils.isIncludeFilterMatched(tranEntryName, includes) )
//                        printLog("isExcludeFilterMatched="+Utils.isExcludeFilterMatched(tranEntryName, excludes) )
//                    }
                    if (isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)) {

                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()){
                                try {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(AnnotationMethodScanner(
                                        logger
                                    ) {
                                        val record = ClassMethodRecord(entryName, it)
                                        WovenInfoUtils.addClassMethodRecords(record)
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
    }

    private fun wovenIntoCode(){
//        logger.error("getClassMethodRecord="+WovenInfoUtils.classMethodRecords)
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path


                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                    if (methodsRecord != null){
                        FileInputStream(file).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord)
                            jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                            ByteArrayInputStream(byteArray).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }else{
                        jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                    }
                }
            }
        }
        allJars.get().forEach { file ->
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
//                    if (jarEntry.isDirectory || entryName.isEmpty() || !entryName.endsWith(_CLASS) || entryName.startsWith("META-INF/")) {
//                        continue
//                    }
                    if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/")) {
                        continue
                    }

                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)

                    if (methodsRecord != null){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord)
                            jarOutput.putNextEntry(JarEntry(entryName))
                            ByteArrayInputStream(byteArray).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }else if (Utils.dotToSlash(Utils.MethodAnnoUtils) + _CLASS == entryName) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val originInject = inputs.readAllBytes()
                            val resultByteArray = RegisterMapWovenInfoCode().execute(ByteArrayInputStream(originInject))
                            jarOutput.putNextEntry(JarEntry(entryName))
                            ByteArrayInputStream(resultByteArray).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    } else{
                        jarOutput.putNextEntry(JarEntry(entryName))
                        jarFile.getInputStream(jarEntry).use {
                            it.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                    }


                } catch (e: Exception) {
                    throw RuntimeException("Merge jar error entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                }
            }
            jarFile.close()
        }
    }

}