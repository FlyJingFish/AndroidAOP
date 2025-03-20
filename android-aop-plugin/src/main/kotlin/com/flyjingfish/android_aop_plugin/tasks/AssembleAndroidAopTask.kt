package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.EntryCache
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceInvokeMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SuspendReturnScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AopTaskUtils
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.getFileClassname
import com.flyjingfish.android_aop_plugin.utils.getRelativePath
import com.flyjingfish.android_aop_plugin.utils.inExcludePackingRules
import com.flyjingfish.android_aop_plugin.utils.inRules
import com.flyjingfish.android_aop_plugin.utils.isJarSignatureRelatedFiles
import com.flyjingfish.android_aop_plugin.utils.toClassPath
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
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
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.system.measureTimeMillis


abstract class AssembleAndroidAopTask : DefaultTask() {

    @get:Input
    abstract var variant :String

    @get:Input
    abstract var isFastDex :Boolean

    @get:Input
    abstract var reflectInvokeMethod :Boolean

    @get:Input
    abstract var reflectInvokeMethodStatic :Boolean

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputDirectory
    abstract val outputDir: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    private var jarOutput: JarOutputStream ?= null
    private val ignoreJar = mutableSetOf<String>()
    private val ignoreJarClassPaths = mutableListOf<File>()
    private lateinit var aopTaskUtils : AopTaskUtils
    private var isSingleClassesJar = false

    private val allJarFiles = mutableListOf<File>()

    private val allDirectoryFiles = mutableListOf<File>()

    @TaskAction
    fun taskAction() {
        isSingleClassesJar = (allDirectoryFiles.isEmpty() && allJarFiles.size == 1) || (allDirectoryFiles.size == 1 && allJarFiles.isEmpty())
        aopTaskUtils = AopTaskUtils(project,variant)
        ClassPoolUtils.release(project)
        ClassFileUtils.debugMode = false
        ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
        ClassFileUtils.reflectInvokeMethodStatic = reflectInvokeMethodStatic
        WovenInfoUtils.isCompile = false
        WovenInfoUtils.checkHasInvokeJson(project, variant)
        WovenInfoUtils.checkHasOverrideJson(project, variant)
        println("AndroidAOP woven info code start")
        ClassFileUtils.outputDir = File(Utils.aopTransformTempDir(project,variant))
        ClassFileUtils.clear()
        ClassFileUtils.outputDir.deleteRecursively()
        ClassFileUtils.outputCacheDir = File(Utils.aopCompileTempInvokeDir(project, variant))
        SuspendReturnScanner.hasSuspendReturn = false
        if (!isFastDex){
            jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(outputFile.get().asFile)))
        }
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        jarOutput?.close()
        println("AndroidAOP woven info code finish, current cost time ${scanTimeCost}ms")

    }


    private fun scanFile() {
        allDirectories.get().forEach { directory ->
            var isJarDirectory = false
            if (directory.asFile.isDirectory){
                var count = 0
                directory.asFile.listFiles()?.let {
                    for (file in it) {
                        if (file.isFile && file.absolutePath.endsWith(".jar")){
                            count++
                            allJarFiles.add(file)
                        }
                    }
                    if (count == it.size){
                        isJarDirectory = true
                    }
                }
            }
            if (!isJarDirectory){
                allDirectoryFiles.add(directory.asFile)
            }
        }

        allJars.get().forEach { file ->
            allJarFiles.add(file.asFile)
        }
        val scanTimeCost1 = measureTimeMillis {
            loadJoinPointConfig()
        }
//        println("scanFile cost time scanTimeCost1 ${scanTimeCost1}ms")
        val scanTimeCost2 = measureTimeMillis {
            searchJoinPointLocation()
        }
//        println("scanFile cost time scanTimeCost2 ${scanTimeCost2}ms")
        val scanTimeCost3 = measureTimeMillis {
            wovenIntoCode()
        }
//        println("scanFile cost time scanTimeCost3 ${scanTimeCost3}ms")
    }

    private fun loadJoinPointConfig(){
        WovenInfoUtils.addBaseClassInfo(project)
        ignoreJar.clear()
        ignoreJarClassPaths.clear()
        allJarFiles.forEach { file ->
            if (isSingleClassesJar){
                ignoreJar.add(file.absolutePath)
                return@forEach
            }
            val jarFile = JarFile(file)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
                    if (entryName.isJarSignatureRelatedFiles()){
                        ignoreJar.add(file.absolutePath)
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
                val destDir = "${temporaryDir.absolutePath}${File.separatorChar}${File(path).name.computeMD5()}"
                val destFile = File(destDir)
                destFile.deleteRecursively()
                Utils.openJar(path,destDir)
                ignoreJarClassPaths.add(destFile)
            }
        }

        fun processFile(file : File,directory:File,directoryPath:String){
            aopTaskUtils.processFileForConfig(file, directory, directoryPath)
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
        allDirectoryFiles.forEach { directory ->
//            printLog("directory.asFile.absolutePath = ${directory.asFile.absolutePath}")
            val directoryPath = directory.absolutePath
            WovenInfoUtils.addClassPath(directory.absolutePath)
            directory.walk().forEach { file ->
                processFile(file,directory,directoryPath)
            }
        }

        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            aopTaskUtils.processJarForConfig(file)
        }
        aopTaskUtils.loadJoinPointConfigEnd(true)
    }

    private fun searchJoinPointLocation(){
        aopTaskUtils.searchJoinPointLocationStart(project)

        val addClassMethodRecords = mutableMapOf<String,ClassMethodRecord>()
        val deleteClassMethodRecords = mutableSetOf<String>()

        fun processFile(file : File,directory:File,directoryPath:String){
            aopTaskUtils.processFileForSearch(file, directory, directoryPath,addClassMethodRecords, deleteClassMethodRecords)
        }

        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                processFile(file, directory, directoryPath)
            }

        }
        allDirectoryFiles.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                processFile(file,directory,directoryPath)
            }
        }
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            aopTaskUtils.processJarForSearch(file, addClassMethodRecords, deleteClassMethodRecords)
        }
        aopTaskUtils.searchJoinPointLocationEnd(addClassMethodRecords, deleteClassMethodRecords)

        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                aopTaskUtils.processFileForSearchSuspend(file,directory,directoryPath)
            }

        }
        allDirectoryFiles.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                aopTaskUtils.processFileForSearchSuspend(file,directory,directoryPath)
            }
        }
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            aopTaskUtils.processJarForSearchSuspend(file)
        }

    }
    private val jarEntryCache = ConcurrentHashMap<String,MutableList<EntryCache>>()
    private fun saveEntryCache(jarFileName:String,jarEntryName: String,inputStream: InputStream){
        if (isFastDex){
            val parts = jarEntryName.split("/")
            val jarName = if (isSingleClassesJar){
                when {
                    parts.size >= 4 -> parts.subList(0, 3).joinToString("/").computeMD5()
                    parts.size > 1 -> parts.dropLast(1).joinToString("/").computeMD5()
                    else -> jarFileName
                }
            }else{
                jarFileName
            }

            val entryCaches = jarEntryCache.computeIfAbsent(jarName) { mutableListOf() }
            val byteArray = inputStream.readAllBytes()
            synchronized(entryCaches){
                entryCaches.add(EntryCache(jarEntryName,byteArray))
            }
        }
    }
    private fun wovenIntoCode() = runBlocking{
        jarEntryCache.clear()
        val invokeStaticClassName = Utils.extraPackage+".Invoke"+project.name.computeMD5()
        WovenInfoUtils.initAllClassName()
        WovenInfoUtils.makeReplaceMethodInfoUse()
//        logger.error("getClassMethodRecord="+WovenInfoUtils.classMethodRecords)
        val hasReplace = WovenInfoUtils.hasReplace()
        val hasReplaceExtendsClass = WovenInfoUtils.hasModifyExtendsClass()
        val newClasses = mutableListOf<ByteArray>()
        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
                val entryName = file.getFileClassname(directory)
                if (AndroidAopConfig.inExcludePackingRules(entryName)) {
                    return
                }
                try {
                    val entryClazzName = entryName.replace(_CLASS,"")
                    val relativePath = file.getRelativePath(directory)
                    val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS,"")
                    val isClassFile = file.name.endsWith(_CLASS)
                    val isWovenInfoCode = isClassFile
                            && AndroidAopConfig.inRules(thisClassName)
                            && !entryClazzName.startsWith("kotlinx/") && !entryClazzName.startsWith("kotlin/")

                    val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                    val isSuspend:Boolean
                    val realMethodsRecord: HashMap<String, MethodRecord>? = if (methodsRecord == null && SuspendReturnScanner.hasSuspendReturn && isWovenInfoCode){
                        isSuspend = true
                        val clazzName = entryName.replace(_CLASS,"")
                        WovenInfoUtils.getAopMethodCutInnerClassInfoInvokeClassInfo(clazzName)
                    }else {
                        isSuspend = false
                        methodsRecord
                    }
                    val jarEntryName: String = relativePath.toClassPath()
                    fun realCopy(){
                        file.inputStream().use {
                            saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                            jarOutput?.saveEntry(jarEntryName,it)
                        }
                    }
                    if (realMethodsRecord != null){
                        FileInputStream(file).use { inputs ->
                            val byteArray = try {
                                WovenIntoCode.modifyClass(inputs.readAllBytes(),realMethodsRecord,hasReplace,invokeStaticClassName,isSuspend)
                            } catch (e: Exception) {
                                realCopy()
                                if (isSuspend){
                                    logger.error("Merge directory error1 entry:[${entryName}], error message:$e,如果这个类是包含必须的切点类，请到Github联系作者")
                                }else{
                                    logger.error("Merge directory error1 entry:[${entryName}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                                }
                                null
                            }
                            byteArray?.let { bytes ->
                                bytes.inputStream().use {
                                    saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                    jarOutput?.saveEntry(jarEntryName,it)
                                }
                                synchronized(newClasses){
                                    newClasses.add(bytes)
                                }
                            }
                        }
                    }else if (Utils.dotToSlash(Utils.JoinAnnoCutUtils) + _CLASS == entryName) {
                        FileInputStream(file).use { inputs ->
                            val originInject = inputs.readAllBytes()
                            val resultByteArray = RegisterMapWovenInfoCode().execute(originInject.inputStream())
                            resultByteArray.inputStream().use {
                                saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                jarOutput?.saveEntry(jarEntryName,it)
                            }
                        }
                    }else{
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
                                                saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                                jarOutput?.saveEntry(jarEntryName,it)
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
                        val hasCollect = WovenInfoUtils.getAopCollectClassMap()[thisClassName] != null
                        if (isWovenInfoCode && hasReplace){
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val newByteArray = aopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                        newByteArray.byteArray.inputStream().use {
                                            saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                            jarOutput?.saveEntry(jarEntryName,it)
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
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val newByteArray = aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
                                        if (newByteArray.modified){
                                            newByteArray.byteArray.inputStream().use {
                                                saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                                jarOutput?.saveEntry(jarEntryName,it)
                                            }
                                        }else{
                                            copy()
                                        }

                                        //                                        newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        copy()
                                    }
                                }else{
                                    copy()
                                }
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
                                            saveEntryCache(directoryPath.computeMD5(),jarEntryName,it)
                                            jarOutput?.saveEntry(jarEntryName,it)
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
                } catch (e: Exception) {
                    logger.error("Merge directory error2 entry:[${entryName}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                }
            }
        }
        val wovenCodeFileJobs1 = mutableListOf<Deferred<Unit>>()
        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().sortedBy {
                it.name.length
            }.forEach { file ->
                val job = async(Dispatchers.IO) {
                    processFile(file, directory, directoryPath)
                }
                wovenCodeFileJobs1.add(job)
            }

        }
        wovenCodeFileJobs1.awaitAll()
        val wovenCodeFileJobs2 = mutableListOf<Deferred<Unit>>()
        allDirectoryFiles.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().sortedBy {
                it.name.length
            }.forEach { file ->
                val job = async(Dispatchers.IO) {
                    processFile(file,directory,directoryPath)
                }
                wovenCodeFileJobs2.add(job)
            }
        }
        wovenCodeFileJobs2.awaitAll()


        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = JarFile(file)
            val enumeration = jarFile.entries()
            val oldJarFileName = file.absolutePath.computeMD5()
            val wovenCodeJarJobs = mutableListOf<Deferred<Unit>>()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
//                    if (jarEntry.isDirectory || entryName.isEmpty() || !entryName.endsWith(_CLASS) || entryName.startsWith("META-INF/")) {
//                        continue
//                    }
                if (jarEntry.isDirectory || AndroidAopConfig.inExcludePackingRules(entryName)) {
                    continue
                }
                fun processJar(){
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
                        val realMethodsRecord: HashMap<String, MethodRecord>? = if (methodsRecord == null && SuspendReturnScanner.hasSuspendReturn && isWovenInfoCode){
                            isSuspend = true
                            WovenInfoUtils.getAopMethodCutInnerClassInfoInvokeClassInfo(entryClazzName)
                        }else {
                            isSuspend = false
                            methodsRecord
                        }

                        fun realCopy(){
                            jarFile.getInputStream(jarEntry).use {
                                saveEntryCache(oldJarFileName,entryName,it)
                                jarOutput?.saveEntry(entryName,it)
                            }
                        }
                        if (realMethodsRecord != null){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val byteArray = try {
                                    WovenIntoCode.modifyClass(inputs.readAllBytes(),realMethodsRecord,hasReplace,invokeStaticClassName,isSuspend)
                                } catch (e: Exception) {
                                    realCopy()
                                    if (isSuspend){
                                        logger.error("Merge jar error1 entry:[${jarEntry.name}], error message:$e,如果这个类是包含必须的切点类，请到Github联系作者")
                                    }else{
                                        logger.error("Merge jar error1 entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                                    }
                                    null
                                }
                                byteArray?.let {
                                    it.inputStream().use {
                                        saveEntryCache(oldJarFileName,entryName,it)
                                        jarOutput?.saveEntry(entryName,it)
                                    }
                                    synchronized(newClasses){
                                        newClasses.add(it)
                                    }
                                }
                            }
                        }else if (Utils.dotToSlash(Utils.JoinAnnoCutUtils) + _CLASS == entryName) {
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val originInject = inputs.readAllBytes()
                                val resultByteArray = RegisterMapWovenInfoCode().execute(originInject.inputStream())
                                resultByteArray.inputStream().use {
                                    saveEntryCache(oldJarFileName,entryName,it)
                                    jarOutput?.saveEntry(entryName,it)
                                }
                            }
                        } else{
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
                                                    saveEntryCache(oldJarFileName,entryName,it)
                                                    jarOutput?.saveEntry(entryName,it)
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
                            val hasCollect = WovenInfoUtils.getAopCollectClassMap()[thisClassName] != null

                            if (isWovenInfoCode && hasReplace){
                                jarFile.getInputStream(jarEntry).use { inputs ->
                                    val byteArray = inputs.readAllBytes()
                                    if (byteArray.isNotEmpty()){
                                        try {
                                            val newByteArray = aopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                            newByteArray.byteArray.inputStream().use {
                                                saveEntryCache(oldJarFileName,entryName,it)
                                                jarOutput?.saveEntry(entryName,it)
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
                                jarFile.getInputStream(jarEntry).use { inputs ->
                                    val byteArray = inputs.readAllBytes()
                                    if (byteArray.isNotEmpty()){
                                        try {
                                            val newByteArray = aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
                                            if (newByteArray.modified){
                                                newByteArray.byteArray.inputStream().use {
                                                    saveEntryCache(oldJarFileName,entryName,it)
                                                    jarOutput?.saveEntry(entryName,it)
                                                }
                                            }else{
                                                copy()
                                            }
//                                            newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            copy()
                                        }
                                    }else{
                                        copy()
                                    }
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
                                                saveEntryCache(oldJarFileName,entryName,it)
                                                jarOutput?.saveEntry(entryName,it)
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
//                    e.printStackTrace()
//                    throw RuntimeException("Merge jar error entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                        logger.error("Merge jar error2 entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                    }
                }
                val job = async(Dispatchers.IO) {
                    processJar()
                }
                wovenCodeJarJobs.add(job)
            }

            wovenCodeJarJobs.awaitAll()
            jarFile.close()
        }
        val oldJarFileName = project.name.computeMD5()
        // 这块耗时比较多
        synchronized(newClasses){
            ClassFileUtils.wovenInfoInvokeClass(newClasses)
        }
        if (!ClassFileUtils.reflectInvokeMethod || ClassFileUtils.reflectInvokeMethodStatic){
            val outputDirJobs = mutableListOf<Deferred<Unit>>()
            for (file in ClassFileUtils.outputDir.walk()) {
                if (file.isFile) {
                    val job = async(Dispatchers.IO) {
                        val className = file.getFileClassname(ClassFileUtils.outputDir)
                        val invokeClassName = Utils.slashToDot(className).replace(_CLASS,"")
                        if (!WovenInfoUtils.containsInvokeClass(invokeClassName)){
                            file.inputStream().use {
                                saveEntryCache(oldJarFileName,className,it)
                                jarOutput?.saveEntry(className,it)
                            }
                        }
                    }
                    outputDirJobs.add(job)
                }
            }
            outputDirJobs.awaitAll()
        }
        val collectDir = File(Utils.aopTransformCollectTempDir(project,variant))
        WovenIntoCode.createCollectClass(collectDir)
        val collectDirJobs = mutableListOf<Deferred<Unit>>()
        for (file in collectDir.walk()) {
            if (file.isFile) {
                val job = async(Dispatchers.IO) {
                    val className = file.getFileClassname(collectDir)
                    val invokeClassName = Utils.slashToDot(className).replace(_CLASS,"")
                    if (!WovenInfoUtils.containsInvokeClass(invokeClassName)){
                        file.inputStream().use {
                            saveEntryCache(oldJarFileName,className,it)
                            jarOutput?.saveEntry(className,it)
                        }
                    }
                }
                collectDirJobs.add(job)
            }
        }
        collectDirJobs.awaitAll()
        if (isFastDex){
            val fastDexJobs = mutableListOf<Deferred<Unit>>()
            val jarOutputs = mutableListOf<JarOutputStream>()
            jarEntryCache.forEach { (jarFileName, caches) ->
                val jarFile = File(outputDir.get().asFile.absolutePath, "$jarFileName.jar")
                val existingEntries = readExistingJarEntries(jarFile)
                var jarChanged = false
                for (cache in caches) {
                    if (cache.isChange(existingEntries)){
                        jarChanged = true
                        break
                    }
                }
                if (jarChanged){
                    val jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(jarFile)))
                    jarOutputs.add(jarOutput)
                    for (cache in caches) {
                        val job = async(Dispatchers.IO) {
                            jarOutput.saveEntry(cache.jarEntryName,cache.byteArray)
                        }
                        fastDexJobs.add(job)
                    }
                }

            }
            fastDexJobs.awaitAll()
            for (jarOutput1 in jarOutputs) {
                jarOutput1.close()
            }
        }
        if (!AndroidAopConfig.debug){
            ClassFileUtils.outputDir.deleteRecursively()
            collectDir.deleteRecursively()
        }
        exportCutInfo()
    }

    private fun JarOutputStream.saveEntry(entryName: String, inputStream: InputStream) {
        synchronized(this){
            putNextEntry(JarEntry(entryName))
            inputStream.copyTo( this)
            closeEntry()
        }

    }

    private fun JarOutputStream.saveEntry(entryName: String, data: ByteArray) {
        synchronized(this){
            putNextEntry(JarEntry(entryName))
            write(data)
            closeEntry()
        }
    }
    private fun EntryCache.isChange(existingEntries: Map<String, ByteArray?>):Boolean {
        val safeEntryName = jarEntryName.removePrefix("/")
        val newData = byteArray
        // 如果 JAR 中已存在相同条目，并且内容相同，则跳过写入
        if (existingEntries[safeEntryName]?.contentEquals(newData) == true) {
            return false
        }
        return true
    }
    private fun readExistingJarEntries(jarFile: File): Map<String, ByteArray?> {
        if (!jarFile.exists()) return emptyMap()

        val entries = mutableMapOf<String, ByteArray>()
        JarFile(jarFile).use { jar ->
            jar.entries().asSequence().forEach { entry ->
                if (!entry.isDirectory) {
                    jar.getInputStream(entry).use { inputStream ->
                        entries[entry.name] = inputStream.readBytes()
                    }
                }
            }
        }
        return entries
    }

    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo()
    }

}