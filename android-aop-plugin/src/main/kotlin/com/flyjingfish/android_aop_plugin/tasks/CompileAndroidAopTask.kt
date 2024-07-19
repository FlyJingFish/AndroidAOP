package com.flyjingfish.android_aop_plugin.tasks

import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.TmpFile
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceInvokeMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.AopTaskUtils
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.checkExist
import com.flyjingfish.android_aop_plugin.utils.printLog
import com.flyjingfish.android_aop_plugin.utils.saveEntry
import com.flyjingfish.android_aop_plugin.utils.saveFile
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
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
        ClassFileUtils.outputCacheDir = File(Utils.aopCompileTempInvokeDir(project, variantName))
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
                val entryClazzName = entryName.replace(_CLASS,"")
                val relativePath = directory.toURI().relativize(file.toURI()).path


                val methodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)
                val isSuspend:Boolean
                val realMethodsRecord: HashMap<String, MethodRecord>? = if (methodsRecord == null){
                    isSuspend = true
                    val clazzName = entryName.replace(_CLASS,"")
                    WovenInfoUtils.getAopMethodCutInnerClassInfoInvokeClassInfo(clazzName)
                }else {
                    isSuspend = false
                    methodsRecord
                }

                val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS,"")
                val hasCollect = WovenInfoUtils.aopCollectClassMap[thisClassName] != null
                val outFile = File(tmpCompileDir.absolutePath+"/"+relativePath)
                fun mkOutFile(){
                    outFile.checkExist()
                    val tmpFile = TmpFile(file,outFile)
                    tempFiles.add(tmpFile)
                }
                if (realMethodsRecord != null){
                    mkOutFile()
                    FileInputStream(file).use { inputs ->
                        val byteArray = WovenIntoCode.modifyClass(inputs.readAllBytes(),realMethodsRecord,hasReplace,isSuspend)
                        byteArray.saveFile(outFile)
                        newClasses.add(byteArray)
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
                    val isClassFile = file.name.endsWith(_CLASS)
                    val isWovenInfoCode = isClassFile
                            && Utils.inConfigRules(thisClassName)
                            && !entryClazzName.startsWith("kotlinx/") && !entryClazzName.startsWith("kotlin/")
                    if (isWovenInfoCode && hasReplace){
                        FileInputStream(file).use { inputs ->
                            val byteArray = inputs.readAllBytes()
                            if (byteArray.isNotEmpty()){
                                try {
                                    val newByteArray = AopTaskUtils.wovenIntoCodeForReplace(byteArray)
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
                        val replaceExtendsClassName = WovenInfoUtils.getModifyExtendsClass(Utils.slashToDotClassName(entryClazzName))
                        if (replaceExtendsClassName !=null){
                            FileInputStream(file).use { inputs ->
                                val byteArray = inputs.readAllBytes()
                                if (byteArray.isNotEmpty()){
                                    try {
                                        val newByteArray = AopTaskUtils.wovenIntoCodeForExtendsClass(byteArray)
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

        allDirectories.forEach { directory ->
            val directoryPath = directory.absolutePath
            directory.walk().sortedBy {
                it.name.length
            }.forEach { file ->
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
    private fun exportCutInfo(){
        if (!AndroidAopConfig.cutInfoJson){
            return
        }
        InitConfig.exportCutInfo(isApp)
    }

}