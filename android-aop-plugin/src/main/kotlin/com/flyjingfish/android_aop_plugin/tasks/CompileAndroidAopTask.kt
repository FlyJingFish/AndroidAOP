package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.TmpFile
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AopTaskUtils
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.checkExist
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.system.measureTimeMillis

class CompileAndroidAopTask(
    private val allJars: MutableList<File>,
    private val allDirectories: MutableList<File>,
    private val output: File,
    private val project: Project,
    private val isApp:Boolean,
    private val tmpCompileDir:File,
    private val tmpJsonFile:File,
    private val variantName:String
) {


    private lateinit var logger: Logger
    fun taskAction() {
        logger = project.logger
        WovenInfoUtils.isCompile = true
        ClassFileUtils.outputDir = output
        ClassFileUtils.clear()
        println("AndroidAOP woven info code start")
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        println("AndroidAOP woven info code finish, current cost time ${scanTimeCost}ms")

    }

    private fun scanFile() {
        loadJoinPointConfig()
        searchJoinPointLocation()
        wovenIntoCode()
    }

    private fun loadJoinPointConfig(){
        WovenInfoUtils.addBaseClassInfo(project)

        //第一遍找配置文件
        allDirectories.forEach { directory ->
//            printLog("directory.asFile.absolutePath = ${directory.asFile.absolutePath}")
            val directoryPath = directory.absolutePath
            WovenInfoUtils.addClassPath(directoryPath)
            directory.walk().forEach { file ->
                AopTaskUtils.processFileForConfig(file, directory, directoryPath)
            }

        }

        allJars.forEach { file ->
           AopTaskUtils.processJarForConfig(file)
        }
        AopTaskUtils.loadJoinPointConfigEnd(isApp)
    }

    private fun searchJoinPointLocation(){
        AopTaskUtils.searchJoinPointLocationStart(project)

        val addClassMethodRecords = mutableMapOf<String,ClassMethodRecord>()
        val deleteClassMethodRecords = mutableSetOf<String>()

        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                AopTaskUtils.processFileForSearch(file, directory, directoryPath,addClassMethodRecords, deleteClassMethodRecords)
            }
        }
        allJars.forEach { file ->
            AopTaskUtils.processJarForSearch(file, addClassMethodRecords, deleteClassMethodRecords)
        }
        AopTaskUtils.searchJoinPointLocationEnd(addClassMethodRecords, deleteClassMethodRecords)
    }
    private fun wovenIntoCode(){
        val includes = AndroidAopConfig.includes
        val excludes = AndroidAopConfig.excludes
        WovenInfoUtils.makeReplaceMethodInfoUse()
//        logger.error("getClassMethodRecord="+WovenInfoUtils.classMethodRecords)
        val hasReplace = WovenInfoUtils.hasReplace()
        val hasReplaceExtendsClass = WovenInfoUtils.hasModifyExtendsClass()
        val tempFiles = mutableListOf<TmpFile>()
        val newClasses = mutableListOf<ByteArray>()
        fun processFile(file : File,directory:File,directoryPath:String){
            if (file.isFile) {
                val entryName = file.absolutePath.replace("$directoryPath/","")
                if (entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    return
                }
                val relativePath = directory.toURI().relativize(file.toURI()).path


                val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS,"")
                val hasCollect = WovenInfoUtils.aopCollectClassMap[thisClassName] != null
                val outFile = File(tmpCompileDir.absolutePath+"/"+relativePath)
                fun mkOutFile(){
                    outFile.checkExist()
                    val tmpFile = TmpFile(file,outFile)
                    tempFiles.add(tmpFile)
                }
                if (methodsRecord != null){
                    mkOutFile()
                    FileInputStream(file).use { inputs ->
                        val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),methodsRecord,hasReplace)
                        ByteArrayInputStream(byteArray).use {
                            outFile.saveEntry(it)
                        }
                        newClasses.add(byteArray)
                    }
                }else{
                    fun copy(){
//                        file.inputStream().use {
//                            outFile.saveEntry(it)
//                        }
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
                                    val newByteArray = AopTaskUtils.wovenIntoCodeForReplace(byteArray)
                                    if (newByteArray.modified){
                                        mkOutFile()
                                        ByteArrayInputStream(newByteArray.byteArray).use {
                                            outFile.saveEntry(it)
                                        }
                                    }
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
                                        val newByteArray = AopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
                                        if (newByteArray.modified){
                                            mkOutFile()
                                            ByteArrayInputStream(newByteArray.byteArray).use {
                                                outFile.saveEntry(it)
                                            }
                                        }
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
                                    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
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
                                            return mv
                                        }
                                    }
                                    cr.accept(cv, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

                                    if (!thisHasStaticClock){
                                        WovenIntoCode.wovenStaticCode(cw, thisClassName)
                                    }

                                    mkOutFile()
                                    val newByteArray = cw.toByteArray()
                                    ByteArrayInputStream(newByteArray).use {
                                        outFile.saveEntry(it)
                                    }
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

        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().forEach { file ->
                processFile(file,directory,directoryPath)
            }
        }
        if (isApp){
            val cacheDeleteFiles = mutableListOf<String>()
            val tmpOtherDir = File(Utils.aopCompileTempOtherDir(project,variantName))
            WovenIntoCode.createInitClass(tmpOtherDir)
            WovenIntoCode.createCollectClass(tmpOtherDir)
            for (file in tmpOtherDir.walk()) {
                if (file.isFile) {
                    val relativePath = tmpOtherDir.toURI().relativize(file.toURI()).path
//                    println("relativePath=$relativePath")
                    val target = File(output.absolutePath + "/" + relativePath)
                    target.checkExist()
                    file.inputStream().use {
                        target.saveEntry(it)
                    }
                    cacheDeleteFiles.add(target.absolutePath)
                }
            }
            InitConfig.exportCacheCutFile(File(Utils.aopCompileTempOtherJson(project,variantName)),cacheDeleteFiles)
            if (!AndroidAopConfig.debug){
                tmpOtherDir.deleteRecursively()
            }
        }

        for (tempFile in tempFiles) {
            tempFile.tmp.inputStream().use {
                tempFile.target.saveEntry(it)
            }
//            tempFile.tmp.copyTo(tempFile.target,true)
        }
        if (!AndroidAopConfig.debug){
            tmpCompileDir.deleteRecursively()
        }
        val cacheFiles = ClassFileUtils.wovenInfoInvokeClass(newClasses)
        InitConfig.exportCacheCutFile(tmpJsonFile,cacheFiles)
        if (isApp){
            exportCutInfo()
        }
    }
    private fun File.saveEntry(inputStream: InputStream) {
        this.outputStream().use {
            inputStream.copyTo(it)
        }
    }
    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo(isApp)
    }

}