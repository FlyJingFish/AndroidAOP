package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
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
import com.flyjingfish.android_aop_plugin.utils.printDetail
import com.flyjingfish.android_aop_plugin.utils.printLog
import com.flyjingfish.android_aop_plugin.utils.toClassPath
import io.github.flyjingfish.fast_transform.tasks.DefaultTransformTask
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis


abstract class AssembleAndroidAopTask : DefaultTransformTask() {

    @get:Input
    abstract var variant :String

    @get:Input
    abstract var reflectInvokeMethod :Boolean

    @get:Input
    abstract var reflectInvokeMethodStatic :Boolean

    private val ignoreJar = mutableSetOf<String>()
    private val ignoreJarClassPaths = mutableListOf<File>()
    private lateinit var aopTaskUtils : AopTaskUtils

    private val allJarFiles = mutableListOf<File>()

    private val allDirectoryFiles = mutableListOf<File>()

    override fun startTask() {
        Utils.logger = logger
        aopTaskUtils = AopTaskUtils(project,variant)
        ClassPoolUtils.release(project)
        ClassPoolUtils.clear()
        ClassFileUtils.debugMode = false
        ClassFileUtils.reflectInvokeMethod = reflectInvokeMethod
        ClassFileUtils.reflectInvokeMethodStatic = reflectInvokeMethodStatic
        WovenInfoUtils.isCompile = false
        WovenInfoUtils.checkHasInvokeJson(project, variant)
        WovenInfoUtils.checkHasOverrideJson(project, variant)
        println("AndroidAOP woven info code start")
        ClassFileUtils.get(project).outputDir = File(Utils.aopTransformTempDir(project,variant))
        ClassFileUtils.get(project).clear()
//        ClassFileUtils.outputDir.deleteRecursively()
        ClassFileUtils.get(project).outputCacheDir = File(Utils.aopCompileTempInvokeDir(project, variant))
        SuspendReturnScanner.hasSuspendReturn = false
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        println("AndroidAOP woven info code finish, current cost time ${scanTimeCost}ms")

    }

    override fun endTask() {
        exportCutInfo()
    }

    private fun scanFile() {
        allJarFiles.addAll(allJars())
        allDirectoryFiles.addAll(allDirectories())
        val scanTimeCost1 = measureTimeMillis {
            loadJoinPointConfig()
        }
        printLog("Step 1 cost ${scanTimeCost1}ms")
        val scanTimeCost2 = measureTimeMillis {
            searchJoinPointLocation()
        }
        printLog("Step 2 cost ${scanTimeCost2}ms")
        val scanTimeCost3 = measureTimeMillis {
            wovenIntoCode()
        }
        printLog("Step 3 cost ${scanTimeCost3}ms")
    }

    private fun loadJoinPointConfig() = runBlocking{
        WovenInfoUtils.clear()
        WovenInfoUtils.addBaseClassInfo(project)
        ignoreJar.clear()
        ignoreJarClassPaths.clear()
        allJarFiles.forEach { file ->
            if (singleClassesJar()){
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

        if (!ClassFileUtils.reflectInvokeMethod){
            WovenInfoUtils.addClassPath(ClassFileUtils.get(project).outputDir.absolutePath)
        }
        val searchJobs = mutableListOf<Deferred<Unit>>()
        fun processFile(file : File,directory:File,directoryPath:String){
            aopTaskUtils.processFileForConfig(file, directory, directoryPath,this@runBlocking,searchJobs)
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
                processFile(file, directory, directoryPath)
            }
        }
        val jarFiles = mutableListOf<JarFile>()
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = aopTaskUtils.processJarForConfig(file,this@runBlocking,searchJobs)
            jarFiles.add(jarFile)
        }
        if (searchJobs.isNotEmpty()){
            searchJobs.awaitAll()
        }
        for (jarFile in jarFiles) {
            withContext(Dispatchers.IO) {
                jarFile.close()
            }
        }
        aopTaskUtils.loadJoinPointConfigEnd(true)
    }

    private fun searchJoinPointLocation() = runBlocking{
        aopTaskUtils.searchJoinPointLocationStart(project)

        val addClassMethodRecords = ConcurrentHashMap<String,ClassMethodRecord>()
        val deleteClassMethodRecords = ConcurrentHashMap.newKeySet<String>()
        val searchJobs1 = mutableListOf<Deferred<Unit>>()
        fun processFile(file : File,directory:File,directoryPath:String){
            aopTaskUtils.processFileForSearch(file, directory, directoryPath,addClassMethodRecords, deleteClassMethodRecords,this@runBlocking,searchJobs1)
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
                processFile(file, directory, directoryPath)
            }
        }
        val jarFiles = mutableListOf<JarFile>()
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }

            jarFiles.add(aopTaskUtils.processJarForSearch(file, addClassMethodRecords, deleteClassMethodRecords,this@runBlocking,searchJobs1))
        }
        searchJobs1.awaitAll()
        aopTaskUtils.searchJoinPointLocationEnd(addClassMethodRecords, deleteClassMethodRecords)
        val searchJobs2 = mutableListOf<Deferred<Unit>>()
        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    aopTaskUtils.processFileForSearchSuspend(file,directory,directoryPath)
                }
                searchJobs2.add(job)
            }

        }
        allDirectoryFiles.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    aopTaskUtils.processFileForSearchSuspend(file,directory,directoryPath)
                }
                searchJobs2.add(job)
            }
        }
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }

            jarFiles.add(aopTaskUtils.processJarForSearchSuspend(file,this@runBlocking,searchJobs2))
        }
        searchJobs2.awaitAll()
        for (jarFile in jarFiles) {
            withContext(Dispatchers.IO) {
                jarFile.close()
            }
        }
    }
    private fun saveEntryCache(jarFile:File,jarEntryName: String,inputStream: InputStream){
        jarFile.saveJarEntry(jarEntryName, inputStream)
    }
    private fun wovenIntoCode() = runBlocking{
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
                            saveEntryCache(directory,jarEntryName,it)
                        }
                    }
                    if (realMethodsRecord != null){
                        FileInputStream(file).use { inputs ->
                            val oldBytes = inputs.readAllBytes()
                            val byteArray = try {
                                WovenIntoCode.modifyClass(project,oldBytes,realMethodsRecord,hasReplace,invokeStaticClassName,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions(),isSuspend)
                            } catch (e: Exception) {
                                e.printDetail()
                                try {
                                    WovenIntoCode.modifyClass(project,oldBytes,realMethodsRecord,hasReplace,invokeStaticClassName,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2(),isSuspend)
                                } catch (e2: Exception) {
                                    realCopy()
                                    if (isSuspend){
                                        logger.error("Merge directory error1 entry:[${entryName}], error message:$e2,如果这个类是包含必须的切点类，请到Github联系作者")
                                    }else{
                                        logger.error("Merge directory error1 entry:[${entryName}], error message:$e2,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                                    }
                                    e2.printDetail()
                                    null
                                }
                            }
                            byteArray?.let { bytes ->
                                bytes.inputStream().use {
                                    saveEntryCache(directory,jarEntryName,it)
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
                                saveEntryCache(directory,jarEntryName,it)
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
                                                saveEntryCache(directory,jarEntryName,it)
                                            }
                                        } catch (e: Exception) {
                                            e.printDetail()
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
                                        val newByteArray = try {
                                            aopTaskUtils.wovenIntoCodeForReplace(byteArray,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions())
                                        } catch (e: Exception) {
                                            aopTaskUtils.wovenIntoCodeForReplace(byteArray,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2())
                                        }
                                        newByteArray.byteArray.inputStream().use {
                                            saveEntryCache(directory,jarEntryName,it)
                                        }
                                        //                                    newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        e.printDetail()
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
                                        val newByteArray = try {
                                            aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions())
                                        } catch (e: Exception) {
                                            aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2())
                                        }
                                        if (newByteArray.modified){
                                            newByteArray.byteArray.inputStream().use {
                                                saveEntryCache(directory,jarEntryName,it)
                                            }
                                        }else{
                                            copy()
                                        }

                                        //                                        newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        e.printDetail()
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
                                            saveEntryCache(directory,jarEntryName,it)
                                        }
                                        //                                    newClasses.add(newByteArray)
                                    } catch (e: Exception) {
                                        e.printDetail()
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
                    e.printDetail()
                }
            }
        }
        val wovenCodeFileJarJobs = mutableListOf<Deferred<Unit>>()
        for (directory in ignoreJarClassPaths) {
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    processFile(file, directory, directoryPath)
                }
                wovenCodeFileJarJobs.add(job)
            }

        }
        allDirectoryFiles.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    processFile(file,directory,directoryPath)
                }
                wovenCodeFileJarJobs.add(job)
            }
        }
//        wovenCodeFileJobs1.awaitAll()

        val closeJarFiles = mutableListOf<JarFile>()
        allJarFiles.forEach { file ->
            if (file.absolutePath in ignoreJar){
                return@forEach
            }
            val jarFile = JarFile(file)
            closeJarFiles.add(jarFile)
            val enumeration = jarFile.entries()
            val oldJarFileName = file
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
                            }
                        }
                        if (realMethodsRecord != null){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val oldBytes = inputs.readAllBytes()
                                val byteArray = try {
                                    WovenIntoCode.modifyClass(project,oldBytes,realMethodsRecord,hasReplace,invokeStaticClassName,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions(),isSuspend)
                                } catch (e: Exception) {
                                    try {
                                        WovenIntoCode.modifyClass(project,oldBytes,realMethodsRecord,hasReplace,invokeStaticClassName,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2(),isSuspend)
                                    } catch (e: Exception) {
                                        realCopy()
                                        if (isSuspend){
                                            logger.error("Merge jar error1 entry:[${jarEntry.name}], error message:$e,如果这个类是包含必须的切点类，请到Github联系作者")
                                        }else{
                                            logger.error("Merge jar error1 entry:[${jarEntry.name}], error message:$e,通常情况下你需要先重启Android Studio,然后clean一下项目即可，如果还有问题请到Github联系作者")
                                        }
                                        e.printDetail()
                                        null
                                    }
                                }
                                byteArray?.let {
                                    it.inputStream().use {
                                        saveEntryCache(oldJarFileName,entryName,it)
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
                                                }
//                                        newClasses.add(newByteArray)
                                            } catch (e: Exception) {
                                                e.printDetail()
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
                                            val newByteArray = try {
                                                aopTaskUtils.wovenIntoCodeForReplace(byteArray,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions())
                                            } catch (e: Exception) {
                                                aopTaskUtils.wovenIntoCodeForReplace(byteArray,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2())
                                            }
                                            newByteArray.byteArray.inputStream().use {
                                                saveEntryCache(oldJarFileName,entryName,it)
                                            }
//                                        newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            e.printDetail()
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
                                            val newByteArray = try {
                                                aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray,WovenInfoUtils.getWovenClassWriterFlags(),WovenInfoUtils.getWovenParsingOptions())
                                            } catch (e: Exception) {
                                                aopTaskUtils.wovenIntoCodeForExtendsClass(byteArray,WovenInfoUtils.getWovenClassWriterFlags2(),WovenInfoUtils.getWovenParsingOptions2())
                                            }
                                            if (newByteArray.modified){
                                                newByteArray.byteArray.inputStream().use {
                                                    saveEntryCache(oldJarFileName,entryName,it)
                                                }
                                            }else{
                                                copy()
                                            }
//                                            newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            e.printDetail()
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
                                            }
//                                        newClasses.add(newByteArray)
                                        } catch (e: Exception) {
                                            e.printDetail()
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
                        e.printDetail()
                    }
                }
                val job = async(Dispatchers.IO) {
                    processJar()
                }
                wovenCodeFileJarJobs.add(job)
//                wovenCodeJarJobs.add(job)
            }

//            wovenCodeJarJobs.awaitAll()
//            jarFile.close()
        }
        wovenCodeFileJarJobs.awaitAll()
        for (jarFile in closeJarFiles) {
            withContext(Dispatchers.IO) {
                jarFile.close()
            }
        }
        val oldJarFileName = project.layout.buildDirectory.asFile.get()
        // 这块耗时比较多
        synchronized(newClasses){
            ClassFileUtils.get(project).wovenInfoInvokeClass(newClasses)
        }
        if (!ClassFileUtils.reflectInvokeMethod || ClassFileUtils.reflectInvokeMethodStatic){
            val outputDirJobs = mutableListOf<Deferred<Unit>>()
            for (file in ClassFileUtils.get(project).outputDir.walk()) {
                if (file.isFile) {
                    val job = async(Dispatchers.IO) {
                        val className = file.getFileClassname(ClassFileUtils.get(project).outputDir)
                        val invokeClassName = Utils.slashToDot(className).replace(_CLASS,"")
                        if (!WovenInfoUtils.containsInvokeClass(invokeClassName)){
                            file.inputStream().use {
                                saveEntryCache(oldJarFileName,className,it)
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
                        }
                    }
                }
                collectDirJobs.add(job)
            }
        }
        collectDirJobs.awaitAll()
//        if (!AndroidAopConfig.debug){
//            ClassFileUtils.outputDir.deleteRecursively()
//            collectDir.deleteRecursively()
//        }

    }

    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo()
    }

}