package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.CutInfo
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.beans.WovenResult
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.ex.AndroidAOPOverrideMethodException
import com.flyjingfish.android_aop_plugin.ex.AndroidAOPReplaceSetErrorException
import com.flyjingfish.android_aop_plugin.scanner_visitor.ClassSuperScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.FixBugClassWriter
import com.flyjingfish.android_aop_plugin.scanner_visitor.MethodReplaceInvokeVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAOPConfigVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAopMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SuspendReturnScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import javassist.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.MethodVisitor
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

class AopTaskUtils(
    private val project: RuntimeProject,
    private val variantName: String,
    private val isAndroidModule: Boolean = true
) {
    fun processFileForConfig(file: File, directory: File, directoryPath: String,scope: CoroutineScope, searchJobs: MutableList<Deferred<Unit>>) {
        if (file.isFile) {
            val className = file.getFileClassname(directory)
            if (!FileHashUtils.isScanFile(1,className)){
                return
            }
            WovenInfoUtils.addClassName(className)
            if (file.name.endsWith(Utils.AOP_CONFIG_END_NAME)) {

                fun processJar(){
                    FileInputStream(file).use { inputs ->
                        val classReader = ClassReader(inputs.readAllBytes())
                        classReader.accept(
                            SearchAOPConfigVisitor(), ClassReader.EXPAND_FRAMES
                        )
                    }
                }
                val job = scope.async(Dispatchers.IO) {
                    processJar()
                }
                searchJobs.add(job)
//                processJar()
            } else if (file.absolutePath.endsWith(_CLASS)) {
                if (AndroidAopConfig.verifyLeafExtends && !className.startsWith("kotlinx/") && !className.startsWith(
                        "kotlin/"
                    )
                ) {

                    fun processJar(){
                        FileInputStream(file).use { inputs ->
                            val bytes = inputs.readAllBytes()
                            if (bytes.isNotEmpty()) {
                                val inAsm = FileHashUtils.isAsmScan(file.absolutePath, bytes, 1)
                                if (inAsm) {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(
                                        ClassSuperScanner(file.absolutePath),
                                        ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                                    )
                                }
                            }
                        }
                    }
                    val job = scope.async(Dispatchers.IO) {
                        processJar()
                    }
                    searchJobs.add(job)
//                    processJar()
                }
            }

        }
    }

    fun processJarForConfig(file: File, scope: CoroutineScope, searchJobs: MutableList<Deferred<Unit>>):JarFile {
        WovenInfoUtils.addClassPath(file.absolutePath)
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                continue
            }
            if (entryName.endsWith(_CLASS)) {
                WovenInfoUtils.addClassName(entryName)
            }
            if (!FileHashUtils.isScanFile(1,entryName)){
                continue
            }
            try {
                if (entryName.endsWith(Utils.AOP_CONFIG_END_NAME)) {
                    fun processJar(){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val classReader = ClassReader(inputs.readAllBytes())
                            classReader.accept(
                                SearchAOPConfigVisitor(), ClassReader.EXPAND_FRAMES
                            )
                        }
                    }
                    val job = scope.async(Dispatchers.IO) {
                        processJar()
                    }
                    searchJobs.add(job)
//                    processJar()
                } else if (entryName.endsWith(_CLASS)) {
                    if (AndroidAopConfig.verifyLeafExtends && !entryName.startsWith("kotlinx/") && !entryName.startsWith(
                            "kotlin/"
                        )
                    ) {
                        fun processJar(){
                            jarFile.getInputStream(jarEntry).use { inputs ->
                                val bytes = inputs.readAllBytes()
                                if (bytes.isNotEmpty()) {
                                    val inAsm = FileHashUtils.isAsmScan(entryName, bytes, 1)
                                    if (inAsm) {
                                        val classReader = ClassReader(bytes)
                                        classReader.accept(
                                            ClassSuperScanner(entryName),
                                            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                                        )
                                    }
                                }
                            }
                        }
                        val job = scope.async(Dispatchers.IO) {
                            processJar()
                        }
                        searchJobs.add(job)
//                        processJar()

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return jarFile
    }

    fun loadJoinPointConfigEnd(isApp: Boolean) {
        WovenInfoUtils.removeDeletedClass()
//        logger.error(""+WovenInfoUtils.aopMatchCuts)
//        InitConfig.saveBuildConfig()
        ClassPoolUtils.initClassPool()
//        ClassPoolUtils.initClassPool(project,variantName)
        FileHashUtils.isChangeAopMatch = WovenInfoUtils.aopMatchsChanged()
        WovenInfoUtils.aopCollectChanged(FileHashUtils.isChangeAopMatch)

        WovenInfoUtils.checkLeafConfig(isApp)
    }

    fun searchJoinPointLocationStart(project: RuntimeProject) {
        if (WovenInfoUtils.isHasExtendsReplace() && isAndroidModule) {
            val androidConfig = project.androidConfig
            val list: List<File> = androidConfig.getBootClasspath()
            for (file in list) {
                try {
                    val jarFile = JarFile(file)
                    val enumeration = jarFile.entries()
                    while (enumeration.hasMoreElements()) {
                        val jarEntry = enumeration.nextElement()
                        try {
                            val entryName = jarEntry.name
                            if (entryName.endsWith(_CLASS)) {
                                val className = entryName.replace(".class", "")
                                WovenInfoUtils.addExtendsReplace(slashToDot(className))
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
    }

    fun processFileForSearch(
        file: File,
        directory: File,
        directoryPath: String,
        addClassMethodRecords: MutableMap<String, ClassMethodRecord>,
        deleteClassMethodRecords: MutableSet<String>,
        scope: CoroutineScope, searchJobs: MutableList<Deferred<Unit>>) {
        if (file.isFile) {
            val isClassFile = file.name.endsWith(_CLASS)
            val entryName = file.getFileClassname(directory)
            val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS, "")
            val className = file.getFileClassname(directory).replace(".class", "")
            if (!FileHashUtils.isScanFile(2,entryName)){
                return
            }
            if (isClassFile && AndroidAopConfig.inRules(thisClassName)) {
                fun processJar(){
                    FileInputStream(file).use { inputs ->
                        val bytes = inputs.readAllBytes()

                        if (bytes.isNotEmpty()) {
                            val inAsm = FileHashUtils.isAsmScan(file.absolutePath, bytes, 2)
                            if (inAsm) {

                                WovenInfoUtils.deleteClassMethodRecord(file.absolutePath)
                                WovenInfoUtils.deleteReplaceMethodInfo(file.absolutePath)
                                try {
                                    val classReader = ClassReader(bytes)
                                    var matchAopMatchCuts: List<AopMatchCut>? = null
                                    classReader.accept(SearchAopMethodVisitor(
                                        object : SearchAopMethodVisitor.OnCallBackMethod {
                                            override fun onBackMatch(aopMatchCuts: List<AopMatchCut>) {
                                                matchAopMatchCuts = aopMatchCuts
                                            }

                                            override fun onBackMethodRecord(methodRecord: MethodRecord) {
                                                val record = ClassMethodRecord(
                                                    file.absolutePath,
                                                    methodRecord
                                                )
//                                                    WovenInfoUtils.addClassMethodRecords(record)
                                                addClassMethodRecords[file.absolutePath + methodRecord.getKey()] =
                                                    record
                                            }

                                            override fun onDeleteMethodRecord(methodRecord: MethodRecord) {
                                                deleteClassMethodRecords.add(file.absolutePath + methodRecord.getKey())
                                            }

                                            override fun onBackReplaceMethodInfo(replaceMethodInfo: ReplaceMethodInfo) {
                                                WovenInfoUtils.addReplaceMethodInfo(
                                                    file.absolutePath,
                                                    replaceMethodInfo
                                                )
                                            }

                                            override fun onThrowOverrideMethod(className: String,overrideMethods:Set<String> ) {
                                                throwOverride(className,overrideMethods)
                                            }

                                            override fun onThrow(throwable: Throwable) {
                                                throw throwable
                                            }
                                        }
                                    ), ClassReader.EXPAND_FRAMES)
                                    processOverride(
                                        slashToDot(className),
                                        matchAopMatchCuts,
                                        entryName
                                    ) {
                                        val record = ClassMethodRecord(file.absolutePath, it)
                                        addClassMethodRecords[file.absolutePath + it.getKey()] = record
                                    }
                                } catch (e: Exception) {
                                    if (e is AndroidAOPOverrideMethodException||e is AndroidAOPReplaceSetErrorException) {
                                        throw e
                                    }
                                    e.printStackTrace()
                                }
                            }

                            WovenInfoUtils.addExtendsReplace(slashToDot(className))

                            val isAopCutClass =
                                WovenInfoUtils.isAopMethodCutClass(className) || WovenInfoUtils.isAopMatchCutClass(
                                    className
                                )
                            if (isAopCutClass && !SuspendReturnScanner.hasSuspendReturn) {
                                val classReader = ClassReader(bytes)
                                classReader.accept(
                                    SuspendReturnScanner(), 0
                                )
                            }
                        }
                    }
                }
                val job = scope.async(Dispatchers.IO) {
                    processJar()
                }
                searchJobs.add(job)

            }

        }

    }

    private fun processOverride(
        className: String,
        matchAopMatchCuts: List<AopMatchCut>?,
        entryName: String,
        addCut: (MethodRecord) -> Unit
    ) {
        matchAopMatchCuts?.filter { it.overrideMethod && !it.isMatchAllMethod() && !it.isMatchPackageName() }
            ?.let {
                val ctClass = try {
                    ClassPoolUtils.classPool?.getCtClass(className) ?: return
                } catch (e: Exception) {
                    return
                }
                val isInterface = Modifier.isInterface(ctClass.modifiers)
                if (isInterface) {
                    return@let
                }
                val allMethods = ctClass.methods.filter { ct ->
                    !Modifier.isStatic(ct.modifiers)
                            && !Modifier.isFinal(ct.modifiers)
                            && !Modifier.isPrivate(ct.modifiers)
                }

                val ctClassName = ctClass.name
                for (aopMatchCut in it) {
                    for (methodName in aopMatchCut.methodNames) {
                        if (!aopMatchCut.isMatchedMethodName(methodName)) {
                            val matchMethodInfo =
                                Utils.getMethodInfo(methodName)
                            for (allMethod in allMethods) {
                                val name = allMethod.name
                                val descriptor = allMethod.signature
                                if (matchMethodInfo != null && name == matchMethodInfo.name) {
                                    val isBack = try {
                                        Utils.verifyMatchCut(descriptor, matchMethodInfo)
                                    } catch (e: Exception) {
                                        true
                                    }
                                    val superMethodClass = allMethod.declaringClass
                                    val ctSuperClassName = superMethodClass.name
                                    val isInnerClass = ctSuperClassName.contains("$")
                                    val isSamePackageName = ctSuperClassName.substring(
                                        0,
                                        ctSuperClassName.lastIndexOf(".")
                                    ) == ctClassName.substring(0, ctClassName.lastIndexOf("."))
                                    val isBackMethod = if (isInnerClass) {
                                        if (Modifier.isStatic(superMethodClass.modifiers) || Modifier.isInterface(
                                                superMethodClass.modifiers
                                            )
                                        ) {
                                            isSamePackageName
                                        } else {
                                            false
                                        }
                                    } else {
                                        isSamePackageName
                                    }

                                    if (isBack && (!Modifier.isPackage(allMethod.modifiers) || isBackMethod)) {
//                                    printLog("processOverride=name=$name,descriptor=$descriptor")
                                        val cutInfo = CutInfo(
                                            "匹配切面",
                                            slashToDot(
                                                entryName.replace(_CLASS, "")
                                            ),
                                            aopMatchCut.cutClassName,
                                            CutMethodJson(name, descriptor, false)
                                        )
                                        val methodRecord = MethodRecord(
                                            name,
                                            descriptor,
                                            mutableSetOf(aopMatchCut.cutClassName),
                                            false,
                                            ConcurrentHashMap<String, CutInfo>().apply {
                                                put(
                                                    UUID.randomUUID().toString(), cutInfo
                                                )
                                            },
                                            true,
                                            superMethodClass.name
                                        )

                                        addCut(methodRecord)
                                    }
                                }
                            }

                        }
                    }
                }
//            for (allMethod in allMethods) {
//                allMethod.declaringClass.detach()
//            }
//            ctClass.detach()
            }
    }

    fun processJarForSearch(
        file: File,
        addClassMethodRecords: MutableMap<String, ClassMethodRecord>,
        deleteClassMethodRecords: MutableSet<String>,
        scope: CoroutineScope, searchJobs: MutableList<Deferred<Unit>>):JarFile
    {
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                continue
            }
            if (!FileHashUtils.isScanFile(2,entryName)){
                continue
            }
            val isClassFile = entryName.endsWith(_CLASS)
//                    printLog("tranEntryName="+tranEntryName)
            val thisClassName = Utils.slashToDotClassName(entryName).replace(_CLASS, "")
            val className = entryName.replace(".class", "")

            try {
                if (isClassFile && AndroidAopConfig.inRules(thisClassName)) {
                    fun processJar(){
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val bytes = inputs.readAllBytes();
                            if (bytes.isNotEmpty()) {
                                val inAsm = FileHashUtils.isAsmScan(entryName, bytes, 2)
                                if (inAsm) {
                                    WovenInfoUtils.deleteClassMethodRecord(entryName)
                                    WovenInfoUtils.deleteReplaceMethodInfo(entryName)
                                    try {
                                        val classReader = ClassReader(bytes)
                                        var matchAopMatchCuts: List<AopMatchCut>? = null
                                        classReader.accept(SearchAopMethodVisitor(
                                            object : SearchAopMethodVisitor.OnCallBackMethod {
                                                override fun onBackMatch(aopMatchCuts: List<AopMatchCut>) {
                                                    matchAopMatchCuts = aopMatchCuts
                                                }

                                                override fun onBackMethodRecord(methodRecord: MethodRecord) {
                                                    val record =
                                                        ClassMethodRecord(entryName, methodRecord)
//                                                    WovenInfoUtils.addClassMethodRecords(record)
                                                    addClassMethodRecords[entryName + methodRecord.getKey()] =
                                                        record
                                                }

                                                override fun onDeleteMethodRecord(methodRecord: MethodRecord) {
                                                    deleteClassMethodRecords.add(entryName + methodRecord.getKey())
                                                }

                                                override fun onBackReplaceMethodInfo(
                                                    replaceMethodInfo: ReplaceMethodInfo
                                                ) {
                                                    WovenInfoUtils.addReplaceMethodInfo(
                                                        entryName,
                                                        replaceMethodInfo
                                                    )
                                                }

                                                override fun onThrowOverrideMethod(className: String,overrideMethods:Set<String> ) {
                                                    throwOverride(className,overrideMethods)
                                                }


                                                override fun onThrow(throwable: Throwable) {
                                                    throw throwable
                                                }
                                            }
                                        ), ClassReader.EXPAND_FRAMES)
                                        processOverride(
                                            slashToDot(className),
                                            matchAopMatchCuts,
                                            entryName
                                        ) {
                                            val record = ClassMethodRecord(entryName, it)
                                            addClassMethodRecords[entryName + it.getKey()] = record
                                        }
                                    } catch (e: Exception) {
                                        if (e is AndroidAOPOverrideMethodException) {
                                            throw e
                                        }
                                        e.printStackTrace()
                                    }
                                }

                                WovenInfoUtils.addExtendsReplace(slashToDot(className))

                                val isAopCutClass =
                                    WovenInfoUtils.isAopMethodCutClass(className) || WovenInfoUtils.isAopMatchCutClass(
                                        className
                                    )
                                if (isAopCutClass && !SuspendReturnScanner.hasSuspendReturn) {
                                    val classReader = ClassReader(bytes)
                                    classReader.accept(
                                        SuspendReturnScanner(), 0
                                    )
                                }


                            }
                        }
                    }
                    val job = scope.async(Dispatchers.IO) {
                        processJar()
                    }
                    searchJobs.add(job)

                }
            } catch (e: Exception) {
                if (e is AndroidAOPOverrideMethodException||e is AndroidAOPReplaceSetErrorException) {
                    throw e
                }
                printLog("Merge jar error entry:[${jarEntry.name}], error message:$e")
            }


        }
        return jarFile
    }

    private fun throwOverride(className: String,overrideMethods:Set<String> ) {
        val isThrow = if (ClassFileUtils.debugMode){
            val ctClass = try {
                ClassPoolUtils.classPool?.getCtClass(className) ?: return
            } catch (e: Exception) {
                return
            }
            val isInterface = Modifier.isInterface(ctClass.modifiers)
            if (isInterface) {
                return
            }
            val allMethods = ctClass.methods.filter { ct ->
                !Modifier.isStatic(ct.modifiers)
                        && !Modifier.isFinal(ct.modifiers)
                        && !Modifier.isPrivate(ct.modifiers)
            }
            val overrideMethodsNew = overrideMethods.toMutableSet()
            val iterator = overrideMethodsNew.iterator()

            while (iterator.hasNext()){
                val methodName = iterator.next()
                val matchMethodInfo = methodName.split("@")
                val overrideMethodName = matchMethodInfo[1]
                val overrideMethodDesc = matchMethodInfo[2]
                for (allMethod in allMethods) {
                    val name = allMethod.name
                    val descriptor = allMethod.signature
                    if (name == overrideMethodName && descriptor == overrideMethodDesc){
                        iterator.remove()
                    }
                }
            }
            overrideMethodsNew.isNotEmpty()
        }else{
            true
        }
        if (isThrow){
            val hintFilePath = Utils.overrideClassFile(project, variantName)
            InitConfig.exportOverrideClassFile(File(hintFilePath), mutableListOf(className))
            throw AndroidAOPOverrideMethodException("重写$className 的相关方法已经改变，需要 clean 后重新编译")
        }
    }


    fun processFileForSearchSuspend(file: File, directory: File, directoryPath: String) {
        if (file.isFile) {
            val entryName = file.getFileClassname(directory)
            if (entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                return
            }
            val realMethodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(file.absolutePath)


            if (realMethodsRecord != null){
                FileInputStream(file).use { inputs ->
                    WovenIntoCode.searchSuspendClass(inputs.readAllBytes(),realMethodsRecord)
                }
            }
        }

    }

    fun processJarForSearchSuspend(
        file: File, scope: CoroutineScope, searchJobs: MutableList<Deferred<Unit>>):JarFile
    {
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                continue
            }
            val realMethodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)

            if (realMethodsRecord != null){
                fun processJar(){
                    try {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            WovenIntoCode.searchSuspendClass(inputs.readAllBytes(),realMethodsRecord)
                        }
                    } catch (e: Exception) {
                        if (e is AndroidAOPOverrideMethodException) {
                            throw e
                        }
                        printLog("Merge jar error entry:[${jarEntry.name}], error message:$e")
                    }
                }
                val job = scope.async(Dispatchers.IO) {
                    processJar()
                }
                searchJobs.add(job)
            }

        }
        return jarFile
    }

    fun searchJoinPointLocationEnd(
        addClassMethodRecords: MutableMap<String, ClassMethodRecord>,
        deleteClassMethodRecords: MutableSet<String>
    ) {
        for (deleteClassMethodRecordKey in deleteClassMethodRecords) {
            addClassMethodRecords.remove(deleteClassMethodRecordKey)
        }
        for (addClassMethodRecord in addClassMethodRecords) {
            WovenInfoUtils.addClassMethodRecords(addClassMethodRecord.value)
        }
        WovenInfoUtils.removeDeletedClassMethodRecord()
        WovenInfoUtils.verifyModifyExtendsClassInfo()

    }

    fun wovenIntoCodeForReplace(byteArray: ByteArray,wovenClassWriterFlags:Int,wovenParsingOptions:Int): WovenResult {
        val cr = ClassReader(byteArray)
        val cw = FixBugClassWriter(cr, wovenClassWriterFlags)
        var thisHasCollect = false
        var thisHasStaticClock = false
        var thisCollectClassName: String? = null
        var replaceResult = false
        val deleteNews = mutableMapOf<String,List<ReplaceMethodInfo>>()

        val cv = object : MethodReplaceInvokeVisitor(cw,wovenClassWriterFlags, wovenParsingOptions) {
            override fun visit(
                version: Int,
                access: Int,
                name: String,
                signature: String?,
                superName: String,
                interfaces: Array<out String>?
            ) {
                super.visit(version, access, name, signature, superName, interfaces)
                thisHasCollect = hasCollect
                thisCollectClassName = thisClassName
            }

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

            override fun visitEnd() {
                super.visitEnd()
                replaceResult = replaced
                if (modifyed) {
                    replaceResult = true
                }
                deleteNews.putAll(mDeleteNews)
            }
        }
        cr.accept(cv, wovenParsingOptions)

        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock) {
                WovenIntoCode.wovenStaticCode(cw, it)
                replaceResult = true
            }
        }
        val newByte = cw.toByteArray()
        val newByte2 = WovenIntoCode.deleteNews(newByte,deleteNews,wovenClassWriterFlags,wovenParsingOptions)
        if (newByte !== newByte2){
            replaceResult = true
        }
        return WovenResult(newByte2, replaceResult)
    }

    fun wovenIntoCodeForExtendsClass(byteArray: ByteArray,wovenClassWriterFlags:Int,wovenParsingOptions:Int): WovenResult {
        val cr = ClassReader(byteArray)
        val cw = FixBugClassWriter(cr, wovenClassWriterFlags)
        var thisHasCollect = false
        var thisHasStaticClock = false
        var thisCollectClassName: String? = null
        var replaceResult = false
        val cv = object : ReplaceBaseClassVisitor(cw) {
            override fun visit(
                version: Int,
                access: Int,
                name: String,
                signature: String?,
                superName: String,
                interfaces: Array<out String>?
            ) {
                super.visit(version, access, name, signature, superName, interfaces)
                thisHasCollect = hasCollect
                thisCollectClassName = thisClassName
                replaceResult = modifyExtendsClassName != null
            }

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

            override fun visitEnd() {
                super.visitEnd()
                if (modifyed) {
                    replaceResult = true
                }
            }
        }

        cr.accept(cv, wovenParsingOptions)

        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock) {
                WovenIntoCode.wovenStaticCode(cw, it)
                replaceResult = true
            }
        }

        return WovenResult(cw.toByteArray(), replaceResult)
    }
}