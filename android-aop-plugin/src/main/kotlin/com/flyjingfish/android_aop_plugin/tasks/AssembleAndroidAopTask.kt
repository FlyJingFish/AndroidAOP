package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceInvokeMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AopTaskUtils
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.inRules
import com.flyjingfish.android_aop_plugin.utils.isJarSignatureRelatedFiles
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
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.system.measureTimeMillis

abstract class AssembleAndroidAopTask : DefaultTask() {

    @get:Input
    abstract var variant :String

    @get:Input
    abstract var reflectInvokeMethod :Boolean

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private lateinit var jarOutput: JarOutputStream
    private val ignoreJar = mutableSetOf<String>()
    private val ignoreJarClassPaths = mutableListOf<File>()
    @TaskAction
    fun taskAction() {
        ClassFileUtils.debugMode = false
        ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
        WovenInfoUtils.isCompile = false
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
        val isClassesJar = allDirectories.get().isEmpty() && allJars.get().size == 1
        WovenInfoUtils.addBaseClassInfo(project)
        ignoreJar.clear()
        ignoreJarClassPaths.clear()
        allJars.get().forEach { file ->
            if (isClassesJar){
                ignoreJar.add(file.asFile.absolutePath)
                return@forEach
            }
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (entryName.isJarSignatureRelatedFiles()){
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
            val temporaryDir = File(Utils.aopTransformIgnoreJarDir(project,variant))
            for (path in ignoreJar) {
                val destDir = "${temporaryDir.absolutePath}/${File(path).name.computeMD5()}"
                val destFile = File(destDir)
                destFile.deleteRecursively()
                Utils.openJar(path,destDir)
                ignoreJarClassPaths.add(destFile)
            }
        }

        fun processFile(file : File,directory:File,directoryPath:String){
            AopTaskUtils.processFileForConfig(file, directory, directoryPath)
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
            AopTaskUtils.processJarForConfig(file.asFile)
        }
        AopTaskUtils.loadJoinPointConfigEnd(true)
    }

    private fun searchJoinPointLocation(){
        AopTaskUtils.searchJoinPointLocationStart(project)

        val addClassMethodRecords = mutableMapOf<String,ClassMethodRecord>()
        val deleteClassMethodRecords = mutableSetOf<String>()

        fun processFile(file : File,directory:File,directoryPath:String){
            AopTaskUtils.processFileForSearch(file, directory, directoryPath,addClassMethodRecords, deleteClassMethodRecords)
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
            AopTaskUtils.processJarForSearch(file.asFile, addClassMethodRecords, deleteClassMethodRecords)
        }
        AopTaskUtils.searchJoinPointLocationEnd(addClassMethodRecords, deleteClassMethodRecords)
    }

    private fun wovenIntoCode(){
        WovenInfoUtils.initAllClassName()
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
                val entryClazzName = entryName.replace(_CLASS,"")
                val relativePath = directory.toURI().relativize(file.toURI()).path
                val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS,"")
                val isClassFile = file.name.endsWith(_CLASS)
                val isWovenInfoCode = isClassFile
                        && AndroidAopConfig.inRules(thisClassName)
                        && !entryClazzName.startsWith("kotlinx/") && !entryClazzName.startsWith("kotlin/")

                val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                val isSuspend:Boolean
                val realMethodsRecord: HashMap<String, MethodRecord>? = if (methodsRecord == null && isWovenInfoCode){
                    isSuspend = true
                    val clazzName = entryName.replace(_CLASS,"")
                    WovenInfoUtils.getAopMethodCutInnerClassInfoInvokeClassInfo(clazzName)
                }else {
                    isSuspend = false
                    methodsRecord
                }
                val jarEntryName: String = relativePath.replace(File.separatorChar, '/')
                if (realMethodsRecord != null){
                    FileInputStream(file).use { inputs ->
                        val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),realMethodsRecord,hasReplace,isSuspend)
                        byteArray.inputStream().use {
                            jarOutput.saveEntry(jarEntryName,it)
                        }
                        newClasses.add(byteArray)
                    }
                }else if (Utils.dotToSlash(Utils.JoinAnnoCutUtils) + _CLASS == entryName) {
                    FileInputStream(file).use { inputs ->
                        val originInject = inputs.readAllBytes()
                        val resultByteArray = RegisterMapWovenInfoCode().execute(originInject.inputStream())
                        resultByteArray.inputStream().use {
                            jarOutput.saveEntry(entryName,it)
                        }
                    }
                }else{
                    fun realCopy(){
                        file.inputStream().use {
                            jarOutput.saveEntry(jarEntryName,it)
                        }
                    }
                    fun copy(){
                        if (WovenInfoUtils.isHasAopMethodCutInnerClassInfo(entryClazzName)){
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val cr = ClassReader(byteArray)
                                        val cw = ClassWriter(cr,0)
                                        val cv = object : ClassVisitor(Opcodes.ASM9, cw) {
                                            lateinit var className:String
                                            lateinit var superClassName:String
                                            override fun visit(
                                                version: Int,
                                                access: Int,
                                                name: String,
                                                signature: String?,
                                                superName: String,
                                                interfaces: Array<out String>?
                                            ) {
                                                className = name
                                                superClassName = superName
                                                super.visit(version, access, name, signature, superName, interfaces)
                                            }
                                            override fun visitMethod(
                                                access: Int,
                                                name: String,
                                                descriptor: String,
                                                signature: String?,
                                                exceptions: Array<String?>?
                                            ): MethodVisitor {
                                                val mv = super.visitMethod(
                                                    access,
                                                    name,
                                                    descriptor,
                                                    signature,
                                                    exceptions
                                                )
                                                return ReplaceInvokeMethodVisitor(mv,className,superClassName)
                                            }
                                        }
                                        cr.accept(cv, 0)

                                        val newByteArray = cw.toByteArray()
                                        newByteArray.inputStream().use {
                                            jarOutput.saveEntry(jarEntryName,it)
                                        }
                                    } catch (e: Exception) {
                                        realCopy()
                                    }
                                }else{
                                    realCopy()
                                }
                            }

                        }else{
                            realCopy()
                        }
                    }
                    val hasCollect = WovenInfoUtils.aopCollectClassMap[thisClassName] != null
                    if (isWovenInfoCode && hasReplace){
                        FileInputStream(file).use { inputs ->
                            val byteArray = inputs.readAllBytes()
                            if (byteArray.isNotEmpty()){
                                try {
                                    val newByteArray = AopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                    newByteArray.byteArray.inputStream().use {
                                        jarOutput.saveEntry(jarEntryName,it)
                                    }
//                                    newClasses.add(newByteArray)
                                } catch (e: Exception) {
                                    copy()
                                }
                            }else{
                                copy()
                            }
                        }
                    }else if (isWovenInfoCode && hasReplaceExtendsClass){
                        val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(Utils.slashToDotClassName(entryClazzName))
                        if (replaceExtendsClassName !=null){
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val newByteArray = AopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
                                        newByteArray.byteArray.inputStream().use {
                                            jarOutput.saveEntry(jarEntryName,it)
                                        }
//                                        newClasses.add(newByteArray)
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
                    }else if (hasCollect) {
                        FileInputStream(file).use { inputs ->
                            val byteArray = inputs.readAllBytes()
                            if (byteArray.isNotEmpty()){
                                try {
                                    val cr = ClassReader(byteArray)
                                    val cw = ClassWriter(cr,0)
                                    var thisHasStaticClock = false
                                    val cv = object : ReplaceBaseClassVisitor(cw) {
                                        override fun visitMethod(
                                            access: Int,
                                            name: String,
                                            descriptor: String,
                                            signature: String?,
                                            exceptions: Array<String?>?
                                        ): MethodVisitor? {
                                            val mv = super.visitMethod(
                                                access,
                                                name,
                                                descriptor,
                                                signature,
                                                exceptions
                                            )
                                            thisHasStaticClock = isHasStaticClock
                                            return ReplaceInvokeMethodVisitor(mv,clazzName,oldSuperName)
                                        }
                                    }
                                    cr.accept(cv, 0)

                                    if (!thisHasStaticClock){
                                        WovenIntoCode.wovenStaticCode(cw, thisClassName)
                                    }

                                    val newByteArray = cw.toByteArray()
                                    newByteArray.inputStream().use {
                                        jarOutput.saveEntry(jarEntryName,it)
                                    }
//                                    newClasses.add(newByteArray)
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
                }
            }
        }

        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().sortedBy {
                it.name.length
            }.forEach { file ->
                processFile(file, directory, directoryPath)
            }

        }
        allDirectories.get().forEach { directory ->
            val directoryPath = directory.asFile.absolutePath
            directory.asFile.walk().sortedBy {
                it.name.length
            }.forEach { file ->
                processFile(file,directory.asFile,directoryPath)
            }
        }
        allJars.get().forEach { file ->
            if (file.asFile.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            val jarEntryList = mutableListOf<JarEntry>()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
//                    if (jarEntry.isDirectory || entryName.isEmpty() || !entryName.endsWith(_CLASS) || entryName.startsWith("META-INF/")) {
//                        continue
//                    }
                if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    continue
                }
                jarEntryList.add(jarEntry)
            }
            jarEntryList.sortedBy {
                it.name.length
            }.forEach { jarEntry ->
                try {
                    val entryName = jarEntry.name
                    val entryClazzName = entryName.replace(_CLASS,"")
                    val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS,"")
                    val isClassFile = entryName.endsWith(_CLASS)
                    val isWovenInfoCode = isClassFile
                            && AndroidAopConfig.inRules(thisClassName)
                            && !entryName.startsWith("kotlinx/") && !entryName.startsWith("kotlin/")
                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)
                    val isSuspend:Boolean
                    val realMethodsRecord: HashMap<String, MethodRecord>? = if (methodsRecord == null && isWovenInfoCode){
                        isSuspend = true
                        WovenInfoUtils.getAopMethodCutInnerClassInfoInvokeClassInfo(entryClazzName)
                    }else {
                        isSuspend = false
                        methodsRecord
                    }


                    if (realMethodsRecord != null){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),realMethodsRecord,hasReplace,isSuspend)
                            byteArray.inputStream().use {
                                jarOutput.saveEntry(entryName,it)
                            }
                            newClasses.add(byteArray)
                        }
                    }else if (Utils.dotToSlash(Utils.JoinAnnoCutUtils) + _CLASS == entryName) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val originInject = inputs.readAllBytes()
                            val resultByteArray = RegisterMapWovenInfoCode().execute(originInject.inputStream())
                            resultByteArray.inputStream().use {
                                jarOutput.saveEntry(entryName,it)
                            }
                        }
                    } else{
                        fun realCopy(){
                            jarFile.getInputStream(jarEntry).use {
                                jarOutput.saveEntry(entryName,it)
                            }
                        }
                        fun copy(){
                            if (WovenInfoUtils.isHasAopMethodCutInnerClassInfo(entryClazzName)){
                                jarFile.getInputStream(jarEntry).use { inputs ->
                                    val byteArray = inputs.readAllBytes()
                                    if (byteArray.isNotEmpty()){
                                        try {
                                            val cr = ClassReader(byteArray)
                                            val cw = ClassWriter(cr,0)
                                            val cv = object : ClassVisitor(Opcodes.ASM9, cw) {
                                                lateinit var className:String
                                                lateinit var superClassName:String
                                                override fun visit(
                                                    version: Int,
                                                    access: Int,
                                                    name: String,
                                                    signature: String?,
                                                    superName: String,
                                                    interfaces: Array<out String>?
                                                ) {
                                                    className = name
                                                    superClassName = superName
                                                    super.visit(version, access, name, signature, superName, interfaces)
                                                }
                                                override fun visitMethod(
                                                    access: Int,
                                                    name: String,
                                                    descriptor: String,
                                                    signature: String?,
                                                    exceptions: Array<String?>?
                                                ): MethodVisitor {
                                                    val mv = super.visitMethod(
                                                        access,
                                                        name,
                                                        descriptor,
                                                        signature,
                                                        exceptions
                                                    )
                                                    return ReplaceInvokeMethodVisitor(mv,className,superClassName)
                                                }
                                            }
                                            cr.accept(cv, 0)

                                            val newByteArray = cw.toByteArray()
                                            newByteArray.inputStream().use {
                                                jarOutput.saveEntry(entryName,it)
                                            }
//                                        newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            realCopy()
                                        }
                                    }else{
                                        realCopy()
                                    }
                                }
                            }else{
                                realCopy()
                            }
                        }
                        val hasCollect = WovenInfoUtils.aopCollectClassMap[thisClassName] != null

                        if (isWovenInfoCode && hasReplace){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val newByteArray = AopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                        newByteArray.byteArray.inputStream().use {
                                            jarOutput.saveEntry(entryName,it)
                                        }
//                                        newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        copy()
                                    }
                                }else{
                                    copy()
                                }
                            }
                        }else if(isWovenInfoCode && hasReplaceExtendsClass){
                            val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(Utils.slashToDotClassName(entryClazzName))
                            if (replaceExtendsClassName !=null){
                                jarFile.getInputStream(jarEntry).use { inputs ->
                                    val byteArray = inputs.readAllBytes()
                                    if (byteArray.isNotEmpty()){
                                        try {
                                            val newByteArray = AopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
                                            newByteArray.byteArray.inputStream().use {
                                                jarOutput.saveEntry(entryName,it)
                                            }
//                                            newClasses.add(newByteArray)
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
                        }else if (hasCollect) {
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val cr = ClassReader(byteArray)
                                        val cw = ClassWriter(cr,0)
                                        var thisHasStaticClock = false
                                        val cv = object : ReplaceBaseClassVisitor(cw) {
                                            override fun visitMethod(
                                                access: Int,
                                                name: String,
                                                descriptor: String,
                                                signature: String?,
                                                exceptions: Array<String?>?
                                            ): MethodVisitor? {
                                                val mv = super.visitMethod(
                                                    access,
                                                    name,
                                                    descriptor,
                                                    signature,
                                                    exceptions
                                                )
                                                thisHasStaticClock = isHasStaticClock
                                                return ReplaceInvokeMethodVisitor(mv,clazzName,oldSuperName)
                                            }
                                        }
                                        cr.accept(cv, 0)

                                        if (!thisHasStaticClock){
                                            WovenIntoCode.wovenStaticCode(cw, thisClassName)
                                        }

                                        val newByteArray = cw.toByteArray()
                                        newByteArray.inputStream().use {
                                            jarOutput.saveEntry(entryName,it)
                                        }
//                                        newClasses.add(newByteArray)
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
                    if (!WovenInfoUtils.containsInvokeClass(invokeClassName)){
                        file.inputStream().use {
                            jarOutput.saveEntry(className,it)
                        }
                    }
                }
            }
        }
        val collectDir = File(Utils.aopTransformCollectTempDir(project,variant))
        WovenIntoCode.createCollectClass(collectDir)
        for (file in collectDir.walk()) {
            if (file.isFile) {
                val relativePath = collectDir.toURI().relativize(file.toURI()).path
                val className = relativePath.replace(File.separatorChar, '/')
                val invokeClassName = Utils.slashToDot(className).replace(_CLASS,"")
                if (!WovenInfoUtils.containsInvokeClass(invokeClassName)){
                    file.inputStream().use {
                        jarOutput.saveEntry(className,it)
                    }
                }
            }
        }
        if (!AndroidAopConfig.debug){
            ClassFileUtils.outputDir.deleteRecursively()
            collectDir.deleteRecursively()
        }
        exportCutInfo()
    }


    private fun JarOutputStream.saveEntry(entryName: String, inputStream: InputStream) {
        putNextEntry(JarEntry(entryName))
        inputStream.copyTo( this)
        closeEntry()
    }

    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo()
    }

}