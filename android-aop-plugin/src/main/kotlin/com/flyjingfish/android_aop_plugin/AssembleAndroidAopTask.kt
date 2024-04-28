package com.flyjingfish.android_aop_plugin

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAopMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAOPConfigVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ClassSuperScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.MethodReplaceInvokeVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AndroidConfig
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.FileHashUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
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

    @get:Input
    abstract var variant :String

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private lateinit var jarOutput: JarOutputStream
    private companion object {
        private const val END_NAME = "\$\$AndroidAopClass.class"
        private const val _CLASS = Utils._CLASS
        private val JAR_SIGNATURE_EXTENSIONS = setOf("SF", "RSA", "DSA", "EC")
    }
    private val ignoreJar = mutableSetOf<String>()
    private val ignoreJarClassPaths = mutableListOf<File>()
    @TaskAction
    fun taskAction() {
        WovenInfoUtils.checkHasInvokeJson(project, variant)
        println("AndroidAOP woven info code start")
        ClassFileUtils.outputDir = File(Utils.aopTransformTempDir(project,variant))
        ClassFileUtils.clear()
        ClassFileUtils.outputDir.deleteRecursively()
        jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        jarOutput.close()
        println("AndroidAOP woven info code finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() {
        loadJoinPointConfig()
        searchJoinPointLocation()
        wovenIntoCode()
    }

    private fun loadJoinPointConfig(){
        WovenInfoUtils.addBaseClassInfo(project)
        ignoreJar.clear()
        ignoreJarClassPaths.clear()
        allJars.get().forEach { file ->
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (isJarSignatureRelatedFiles(entryName)){
                        ignoreJar.add(file.asFile.absolutePath)
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            jarFile.close()
        }
        if (ignoreJar.isNotEmpty()){
            val temporaryDir = File(project.buildDir.absolutePath + "/tmp/android-aop")
            for (path in ignoreJar) {
                val destDir = "${temporaryDir.absolutePath}/${Utils.computeMD5(File(path).name)}"
                val destFile = File(destDir)
                destFile.deleteRecursively()
                Utils.openJar(path,destDir)
                ignoreJarClassPaths.add(destFile)
            }
        }

        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
//                    logger.error("file.name="+file.absolutePath)
                if (file.name.endsWith(END_NAME)) {
                    FileInputStream(file).use { inputs ->
                        val classReader = ClassReader(inputs.readAllBytes())
                        classReader.accept(
                            SearchAOPConfigVisitor(
                                logger
                            ), ClassReader.EXPAND_FRAMES)
                    }
                }else if (file.absolutePath.endsWith(_CLASS)){
                    val className = file.absolutePath.replace("$directoryPath/","")
                    WovenInfoUtils.addClassName(className)
                    if (AndroidAopConfig.verifyLeafExtends && !className.startsWith("kotlinx/") && !className.startsWith("kotlin/")){
                        FileInputStream(file).use { inputs ->
                            val bytes = inputs.readAllBytes()
                            if (bytes.isNotEmpty()){
                                val inAsm = FileHashUtils.isAsmScan(file.absolutePath,bytes,1)
                                if (inAsm){
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(
                                        ClassSuperScanner(file.absolutePath), ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                                }
                            }
                        }
                    }
                }

            }
        }
        if (!ClassFileUtils.reflectInvokeMethod){
            WovenInfoUtils.addClassPath(ClassFileUtils.outputDir.absolutePath)
        }
        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            WovenInfoUtils.addClassPath(directoryPath)
            directory.walk().forEach { file ->
                processFile(file, directory, directoryPath)
            }

        }

        //第一遍找配置文件
        allDirectories.get().forEach { directory ->
//            printLog("directory.asFile.absolutePath = ${directory.asFile.absolutePath}")
            val directoryPath = directory.asFile.absolutePath
            WovenInfoUtils.addClassPath(directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                processFile(file,directory.asFile,directoryPath)
            }

        }

        allJars.get().forEach { file ->
            if (file.asFile.absolutePath in ignoreJar){
                return@forEach
            }
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
                                SearchAOPConfigVisitor(
                                    logger
                                ), ClassReader.EXPAND_FRAMES)
                        }
                    }else if (entryName.endsWith(_CLASS)){
                        if (AndroidAopConfig.verifyLeafExtends && !entryName.startsWith("kotlinx/") && !entryName.startsWith("kotlin/")){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val bytes = inputs.readAllBytes()
                                if (bytes.isNotEmpty()){
                                    val inAsm = FileHashUtils.isAsmScan(entryName,bytes,1)
                                    if (inAsm){
                                        val classReader = ClassReader(bytes)
                                        classReader.accept(
                                            ClassSuperScanner(entryName), ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
//                    if (!(e is ZipException && e.message?.startsWith("duplicate entry:") == true)) {
//                        logger.warn("Merge jar error entry:[${jarEntry.name}], error message:$e")
//                    }
                }
            }
            jarFile.close()
        }
        WovenInfoUtils.removeDeletedClass()
//        logger.error(""+WovenInfoUtils.aopMatchCuts)
//        InitConfig.saveBuildConfig()
        ClassPoolUtils.initClassPool()
        FileHashUtils.isChangeAopMatch = WovenInfoUtils.aopMatchsChanged()
    }

    private fun searchJoinPointLocation(){
        if (WovenInfoUtils.isHasExtendsReplace()){
            val androidConfig = AndroidConfig(project)
            val list: List<File> = androidConfig.getBootClasspath()
            for (file in list) {
                try {
                    val jarFile = JarFile(file)
                    val enumeration = jarFile.entries()
                    while (enumeration.hasMoreElements()) {
                        val jarEntry = enumeration.nextElement()
                        try {
                            val entryName = jarEntry.name
                            if (entryName.endsWith(Utils._CLASS)) {
                                val className = entryName.replace(".class","")
                                WovenInfoUtils.addExtendsReplace(Utils.slashToDot(className))
                            }
                        } catch (_: Exception) {

                        }
                    }
                    jarFile.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val includes = AndroidAopConfig.includes
        val excludes = AndroidAopConfig.excludes
        val addClassMethodRecords = mutableMapOf<String,ClassMethodRecord>()
        val deleteClassMethodRecords = mutableSetOf<String>()

        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
                val isClassFile = file.name.endsWith(_CLASS)
                val tranEntryName = file.absolutePath.replace("/", ".")
                    .replace("\\", ".")
//                    printLog("tranEntryName="+tranEntryName)
                if (isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)) {
                    FileInputStream(file).use { inputs ->
                        val bytes = inputs.readAllBytes()

                        if (bytes.isNotEmpty()){
                            val inAsm = FileHashUtils.isAsmScan(file.absolutePath,bytes,2)
                            if (inAsm){

                                WovenInfoUtils.deleteClassMethodRecord(file.absolutePath)
                                WovenInfoUtils.deleteReplaceMethodInfo(file.absolutePath)
                                try {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(SearchAopMethodVisitor(
                                        object :SearchAopMethodVisitor.OnCallBackMethod{
                                            override fun onBackMethodRecord(methodRecord: MethodRecord) {
                                                val record = ClassMethodRecord(file.absolutePath, methodRecord)
//                                                    WovenInfoUtils.addClassMethodRecords(record)
                                                addClassMethodRecords[file.absolutePath+methodRecord.getKey()]  = record
                                            }

                                            override fun onDeleteMethodRecord(methodRecord: MethodRecord) {
                                                deleteClassMethodRecords.add(file.absolutePath+methodRecord.getKey())
                                            }

                                            override fun onBackReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo) {
                                                WovenInfoUtils.addReplaceMethodInfo(file.absolutePath, replaceMethodInfo)
                                            }
                                        }
                                    ), ClassReader.EXPAND_FRAMES)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }

                if (file.absolutePath.endsWith(_CLASS)){
                    val className = file.absolutePath.replace("$directoryPath/","").replace(".class","")
                    WovenInfoUtils.addExtendsReplace(Utils.slashToDot(className))
                }
            }
        }

        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                processFile(file, directory, directoryPath)
            }

        }
        allDirectories.get().forEach { directory ->
            val directoryPath = directory.asFile.absolutePath
            directory.asFile.walk().forEach { file ->
                processFile(file,directory.asFile,directoryPath)
            }
        }
        allJars.get().forEach { file ->
            if (file.asFile.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                        continue
                    }
                    val isClassFile = entryName.endsWith(_CLASS)
                    val tranEntryName = entryName.replace("/", ".")
                        .replace("\\", ".")
//                    printLog("tranEntryName="+tranEntryName)
                    if (isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)) {

                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()){
                                val inAsm = FileHashUtils.isAsmScan(entryName,bytes,2)
                                if (inAsm){
                                    WovenInfoUtils.deleteClassMethodRecord(entryName)
                                    WovenInfoUtils.deleteReplaceMethodInfo(entryName)
                                    try {
                                        val classReader = ClassReader(bytes)
                                        classReader.accept(SearchAopMethodVisitor(
                                            object :SearchAopMethodVisitor.OnCallBackMethod{
                                                override fun onBackMethodRecord(methodRecord: MethodRecord) {
                                                    val record = ClassMethodRecord(entryName, methodRecord)
//                                                    WovenInfoUtils.addClassMethodRecords(record)
                                                    addClassMethodRecords[entryName+methodRecord.getKey()]  = record
                                                }

                                                override fun onDeleteMethodRecord(methodRecord: MethodRecord) {
                                                    deleteClassMethodRecords.add(entryName+methodRecord.getKey())
                                                }

                                                override fun onBackReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo) {
                                                    WovenInfoUtils.addReplaceMethodInfo(entryName, replaceMethodInfo)
                                                }
                                            }
                                        ), ClassReader.EXPAND_FRAMES)
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        }
                    }
                    if (entryName.endsWith(_CLASS)){
                        val className = entryName.replace(".class","")
                        WovenInfoUtils.addExtendsReplace(Utils.slashToDot(className))
                    }
                } catch (e: Exception) {
                    if (!(e is ZipException && e.message?.startsWith("duplicate entry:") == true)) {
                        logger.error("Merge jar error entry:[${jarEntry.name}], error message:$e")
                    }
                }
            }
            jarFile.close()
        }
        for (deleteClassMethodRecordKey in deleteClassMethodRecords) {
            addClassMethodRecords.remove(deleteClassMethodRecordKey)
        }
        for (addClassMethodRecord in addClassMethodRecords) {
            WovenInfoUtils.addClassMethodRecords(addClassMethodRecord.value)
        }
        WovenInfoUtils.removeDeletedClassMethodRecord()
        WovenInfoUtils.verifyModifyExtendsClassInfo()
    }

    private fun wovenIntoCode(){
        WovenInfoUtils.initAllClassName()
        val includes = AndroidAopConfig.includes
        val excludes = AndroidAopConfig.excludes
        WovenInfoUtils.makeReplaceMethodInfoUse()
//        logger.error("getClassMethodRecord="+WovenInfoUtils.classMethodRecords)
        val hasReplace = WovenInfoUtils.hasReplace()
        val hasReplaceExtendsClass = WovenInfoUtils.hasModifyExtendsClass()
        val newClasses = mutableListOf<ByteArray>()
        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
                val entryName = file.absolutePath.replace("$directoryPath/","")
                if (entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    return
                }
                val relativePath = directory.toURI().relativize(file.toURI()).path


                val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                if (methodsRecord != null){
                    FileInputStream(file).use { inputs ->
                        val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord,hasReplace)
                        jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                        ByteArrayInputStream(byteArray).use {
                            it.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                        newClasses.add(byteArray)
                    }
                }else{
                    fun copy(){
                        jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                    }
                    val isClassFile = file.name.endsWith(_CLASS)
                    val tranEntryName = file.absolutePath.replace("/", ".")
                        .replace("\\", ".")
                    val isWovenInfoCode = isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)
                    val className = file.absolutePath.replace("$directoryPath/","")
                    if (isWovenInfoCode && hasReplace && !className.startsWith("kotlinx/") && !className.startsWith("kotlin/")){
                        FileInputStream(file).use { inputs ->
                            val byteArray = inputs.readAllBytes()
                            if (byteArray.isNotEmpty()){
                                try {
                                    val cr = ClassReader(byteArray)
                                    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
                                    val cv = MethodReplaceInvokeVisitor(cw)
                                    cr.accept(cv, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                                    val newByteArray = cw.toByteArray()
                                    jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                                    ByteArrayInputStream(newByteArray).use {
                                        it.copyTo(jarOutput)
                                    }
                                    jarOutput.closeEntry()
                                    newClasses.add(newByteArray)
                                } catch (e: Exception) {
                                    copy()
                                }
                            }else{
                                copy()
                            }
                        }
                    }else if (isWovenInfoCode && hasReplaceExtendsClass && !className.startsWith("kotlinx/") && !className.startsWith("kotlin/")){
                        val clazzName = className.replace(_CLASS,"")
                        val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(Utils.slashToDotClassName(clazzName))
                        if (replaceExtendsClassName !=null){
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val cr = ClassReader(byteArray)
                                        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
                                        val cv = ReplaceBaseClassVisitor(cw)
                                        cr.accept(cv, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
                                        val newByteArray = cw.toByteArray()
                                        jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                                        ByteArrayInputStream(newByteArray).use {
                                            it.copyTo(jarOutput)
                                        }
                                        jarOutput.closeEntry()
                                        newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        copy()
                                    }
                                }else{
                                    copy()
                                }
                            }
                        }else{
                            copy()
                        }
                    }else{
                        copy()
                    }
                }
            }
        }

        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                processFile(file, directory, directoryPath)
            }

        }
        allDirectories.get().forEach { directory ->
            val directoryPath = directory.asFile.absolutePath
            directory.asFile.walk().forEach { file ->
                processFile(file,directory.asFile,directoryPath)
            }
        }
        allJars.get().forEach { file ->
            if (file.asFile.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
//                    if (jarEntry.isDirectory || entryName.isEmpty() || !entryName.endsWith(_CLASS) || entryName.startsWith("META-INF/")) {
//                        continue
//                    }
                    if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                        continue
                    }

                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)

                    if (methodsRecord != null){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord,hasReplace)
                            jarOutput.putNextEntry(JarEntry(entryName))
                            ByteArrayInputStream(byteArray).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                            newClasses.add(byteArray)
                        }
                    }else if (Utils.dotToSlash(Utils.JoinAnnoCutUtils) + _CLASS == entryName) {
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
                        fun copy(){
                            jarOutput.putNextEntry(JarEntry(entryName))
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                        val isClassFile = entryName.endsWith(_CLASS)
                        val tranEntryName = entryName.replace("/", ".")
                            .replace("\\", ".")
                        val isWovenInfoCode = isClassFile && Utils.isIncludeFilterMatched(tranEntryName, includes) && !Utils.isExcludeFilterMatched(tranEntryName, excludes)

                        if (isWovenInfoCode && hasReplace && !entryName.startsWith("kotlinx/") && !entryName.startsWith("kotlin/")){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val cr = ClassReader(byteArray)
                                        val cw = ClassWriter(cr, 0)
                                        val cv = MethodReplaceInvokeVisitor(cw)
                                        cr.accept(cv, 0)
                                        val newByteArray = cw.toByteArray()
                                        jarOutput.putNextEntry(JarEntry(entryName))
                                        ByteArrayInputStream(newByteArray).use {
                                            it.copyTo(jarOutput)
                                        }
                                        jarOutput.closeEntry()
                                        newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        copy()
                                    }
                                }else{
                                    copy()
                                }
                            }
                        }else if(isWovenInfoCode && hasReplaceExtendsClass && !entryName.startsWith("kotlinx/") && !entryName.startsWith("kotlin/")){
                            val clazzName = entryName.replace(_CLASS,"")
                            val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(Utils.slashToDotClassName(clazzName))
                            if (replaceExtendsClassName !=null){
                                jarFile.getInputStream(jarEntry).use { inputs ->
                                    val byteArray = inputs.readAllBytes()
                                    if (byteArray.isNotEmpty()){
                                        try {
                                            val cr = ClassReader(byteArray)
                                            val cw = ClassWriter(cr, 0)
                                            val cv = ReplaceBaseClassVisitor(cw)
                                            cr.accept(cv, 0)
                                            val newByteArray = cw.toByteArray()
                                            jarOutput.putNextEntry(JarEntry(entryName))
                                            ByteArrayInputStream(newByteArray).use {
                                                it.copyTo(jarOutput)
                                            }
                                            jarOutput.closeEntry()
                                            newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            copy()
                                        }
                                    }else{
                                        copy()
                                    }
                                }
                            }else{
                                copy()
                            }
                        }else{
                            copy()
                        }


                    }


                } catch (e: Exception) {
//                    throw RuntimeException("Merge jar error entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                    logger.error("Merge jar error entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                }
            }
            jarFile.close()
        }
        ClassFileUtils.wovenInfoInvokeClass(newClasses)
        if (!ClassFileUtils.reflectInvokeMethod){
            for (file in ClassFileUtils.outputDir.walk()) {
                if (file.isFile) {
                    val relativePath = ClassFileUtils.outputDir.toURI().relativize(file.toURI()).path
                    val className = relativePath.replace(File.separatorChar, '/')
                    val invokeClassName = Utils.slashToDot(className).replace(_CLASS,"")
                    WovenInfoUtils.checkHasInvokeClass(invokeClassName)
                    jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(jarOutput)
                    }
                    jarOutput.closeEntry()
                }
            }
        }
        if (!AndroidAopConfig.debug){
            ClassFileUtils.outputDir.deleteRecursively()
        }
        exportCutInfo()
    }


    private fun isJarSignatureRelatedFiles(name: String): Boolean {
        return name.startsWith("META-INF/") && name.substringAfterLast('.') in JAR_SIGNATURE_EXTENSIONS
    }

    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo()
    }

}