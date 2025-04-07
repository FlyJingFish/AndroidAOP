package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.TmpFile
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceInvokeMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SuspendReturnScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AopTaskUtils
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.FileHashUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.checkExist
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.getFileClassname
import com.flyjingfish.android_aop_plugin.utils.getRelativePath
import com.flyjingfish.android_aop_plugin.utils.inRules
import com.flyjingfish.android_aop_plugin.utils.printLog
import com.flyjingfish.android_aop_plugin.utils.saveEntry
import com.flyjingfish.android_aop_plugin.utils.saveFile
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.system.measureTimeMillis

class CompileAndroidAopTask(
    private val allJars: List<File>,
    private val allDirectories: List<File>,
    private val output: File,
    private val project: Project,
    private val isApp:Boolean,
    private val tmpCompileDir:File,
    private val tmpJsonFile:File,
    private val variantName:String,
    private val isAndroidModule:Boolean = true
) {
    private val aopTaskUtils = AopTaskUtils(project,variantName,isAndroidModule)

    private lateinit var logger: Logger
    fun taskAction() {
        ClassPoolUtils.release(project)
        WovenInfoUtils.checkHasOverrideJson(project, variantName)
        logger = project.logger
        WovenInfoUtils.isCompile = true
        ClassFileUtils.get(project).outputDir = output
        ClassFileUtils.get(project).outputCacheDir = File(Utils.aopCompileTempInvokeDir(project, variantName))
        ClassFileUtils.get(project).outputDebugModeCacheDir = File(Utils.aopCompileTempInvokeCacheDir(project, variantName))
        ClassFileUtils.get(project).clear()
        println("AndroidAOP woven info code start")
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        println("AndroidAOP woven info code finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() {
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
        if (isAndroidModule){
            WovenInfoUtils.addBaseClassInfo(project)
        }
        val searchJobs = mutableListOf<Deferred<Unit>>()
        //第一遍找配置文件
        allDirectories.forEach { directory ->
//            printLog("directory.asFile.absolutePath = ${directory.asFile.absolutePath}")
            val directoryPath = directory.absolutePath
            WovenInfoUtils.addClassPath(directoryPath)
            directory.walk().forEach { file ->
                aopTaskUtils.processFileForConfig(file, directory, directoryPath,this@runBlocking,searchJobs)
            }

        }
        val jarFiles = mutableListOf<JarFile>()
        allJars.forEach { file ->
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
        aopTaskUtils.loadJoinPointConfigEnd(isApp)
    }

    private fun searchJoinPointLocation() = runBlocking{
        aopTaskUtils.searchJoinPointLocationStart(project)

        val addClassMethodRecords = ConcurrentHashMap<String,ClassMethodRecord>()
        val deleteClassMethodRecords = ConcurrentHashMap.newKeySet<String>()
        val searchJobs1 = mutableListOf<Deferred<Unit>>()
        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                aopTaskUtils.processFileForSearch(file, directory, directoryPath,addClassMethodRecords, deleteClassMethodRecords,this@runBlocking,searchJobs1)

            }
        }
        val jarFiles = mutableListOf<JarFile>()
        allJars.forEach { file ->
            jarFiles.add(aopTaskUtils.processJarForSearch(file, addClassMethodRecords, deleteClassMethodRecords,this@runBlocking,searchJobs1))
        }
        searchJobs1.awaitAll()
        aopTaskUtils.searchJoinPointLocationEnd(addClassMethodRecords, deleteClassMethodRecords)
        val searchJobs2 = mutableListOf<Deferred<Unit>>()
        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    aopTaskUtils.processFileForSearchSuspend(file, directory, directoryPath)
                }
                searchJobs2.add(job)
            }
        }
        searchJobs2.awaitAll()
        for (jarFile in jarFiles) {
            withContext(Dispatchers.IO) {
                jarFile.close()
            }
        }
    }
    private fun wovenIntoCode() = runBlocking{
        val invokeStaticClassName = Utils.extraPackage+".Invoke"+project.name.computeMD5()
        WovenInfoUtils.makeReplaceMethodInfoUse()
//        logger.error("getClassMethodRecord="+WovenInfoUtils.classMethodRecords)
        val hasReplace = WovenInfoUtils.hasReplace()
        val hasReplaceExtendsClass = WovenInfoUtils.hasModifyExtendsClass()
        val tempFiles = mutableListOf<TmpFile>()
        val newClasses = mutableListOf<ByteArray>()
        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
                val entryName = file.getFileClassname(directory)
                if (entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    return
                }
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


                val hasCollect = WovenInfoUtils.getAopCollectClassMap()[thisClassName] != null
                val outFile = File(tmpCompileDir.absolutePath+File.separatorChar+relativePath)
                fun mkOutFile(){
                    outFile.checkExist()
                    val tmpFile = TmpFile(file,outFile)
                    synchronized(tempFiles){
                        tempFiles.add(tmpFile)
                    }
                }
                if (realMethodsRecord != null){
                    mkOutFile()
                    FileInputStream(file).use { inputs ->
                        val byteArray = WovenIntoCode.modifyClass(project,inputs.readAllBytes(),realMethodsRecord,hasReplace,invokeStaticClassName,isSuspend)
                        byteArray.saveFile(outFile)
                        synchronized(newClasses){
                            newClasses.add(byteArray)
                        }
                    }
                }else{
                    fun copy(){
//                        file.inputStream().use {
//                            outFile.saveEntry(it)
//                        }
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

                                        mkOutFile()
                                        cw.toByteArray().saveFile(outFile)
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }

                    if (isWovenInfoCode && hasReplace){
                        FileInputStream(file).use { inputs ->
                            val byteArray = inputs.readAllBytes()
                            if (byteArray.isNotEmpty()){
                                try {
                                    val newByteArray = aopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                    if (newByteArray.modified){
                                        mkOutFile()
                                        newByteArray.byteArray.saveFile(outFile)
                                    }
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
                                        mkOutFile()
                                        newByteArray.byteArray.saveFile(outFile)
                                    }
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

                                    mkOutFile()
                                    cw.toByteArray().saveFile(outFile)
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
        val wovenCodeJobs = mutableListOf<Deferred<Unit>>()
        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                val job = async(Dispatchers.IO) {
                    processFile(file,directory,directoryPath)
                }
                wovenCodeJobs.add(job)
            }
        }
        wovenCodeJobs.awaitAll()
        if (isApp){
            val cacheDeleteFiles = mutableListOf<String>()
            val tmpOtherDir = File(Utils.aopCompileTempOtherDir(project,variantName))
            WovenIntoCode.createInitClass(tmpOtherDir)
            WovenIntoCode.createCollectClass(tmpOtherDir)
            val tmpOtherJobs = mutableListOf<Deferred<Unit>>()
            for (file in tmpOtherDir.walk()) {
                if (file.isFile) {
                    val job = async(Dispatchers.IO) {
                        val relativePath = file.getRelativePath(tmpOtherDir)

//                    println("relativePath=$relativePath")
                        val target = File(output.absolutePath + File.separatorChar + relativePath)
                        target.checkExist()
                        synchronized(cacheDeleteFiles){
                            cacheDeleteFiles.add(target.absolutePath)
                        }
                        file.inputStream().use {
                            target.saveEntry(it)
                        }

                    }
                    tmpOtherJobs.add(job)
                }
            }
            tmpOtherJobs.awaitAll()
            InitConfig.exportCacheCutFile(File(Utils.aopCompileTempOtherJson(project,variantName)),cacheDeleteFiles)
            if (!AndroidAopConfig.debug){
                tmpOtherDir.deleteRecursively()
            }
        }
        val tempFileJobs = mutableListOf<Deferred<Unit>>()
        for (tempFile in tempFiles) {
            val job = async(Dispatchers.IO) {
                tempFile.tmp.inputStream().use {
                    tempFile.target.saveEntry(it)
                }
            }
            tempFileJobs.add(job)
//            tempFile.tmp.copyTo(tempFile.target,true)
        }
        tempFileJobs.awaitAll()
//        if (!AndroidAopConfig.debug){
//            tmpCompileDir.deleteRecursively()
//        }
        synchronized(newClasses){
            val cacheFiles = ClassFileUtils.get(project).wovenInfoInvokeClass(newClasses)
            InitConfig.exportCacheCutFile(tmpJsonFile,cacheFiles)
        }
        if (isApp){
            exportCutInfo()
            FileHashUtils.clearScanRecord()
            WovenInfoUtils.clear()
            SuspendReturnScanner.hasSuspendReturn = false
        }
    }
    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo(isApp)
    }

}