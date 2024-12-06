package com.flyjingfish.android_aop_plugin.utils

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.ClassMethodRecord
import com.flyjingfish.android_aop_plugin.beans.CutInfo
import com.flyjingfish.android_aop_plugin.beans.CutMethodJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.beans.TmpFile
import com.flyjingfish.android_aop_plugin.beans.WovenResult
import com.flyjingfish.android_aop_plugin.config.AndroidAopConfig
import com.flyjingfish.android_aop_plugin.ex.AndroidAOPOverrideMethodException
import com.flyjingfish.android_aop_plugin.scanner_visitor.ClassSuperScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.MethodReplaceInvokeVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.RegisterMapWovenInfoCode
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceBaseClassVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.ReplaceInvokeMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAOPConfigVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SearchAopMethodVisitor
import com.flyjingfish.android_aop_plugin.scanner_visitor.SuspendReturnScanner
import com.flyjingfish.android_aop_plugin.scanner_visitor.WovenIntoCode
import com.flyjingfish.android_aop_plugin.utils.Utils._CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDot
import javassist.Modifier
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import java.util.jar.JarFile

class AopTaskUtils(
    private val project: Project,
    private val variantName: String,
    private val isAndroidModule: Boolean = true
) {
    fun processFileForConfig(file: File, directory: File, directoryPath: String) {
        if (file.isFile) {
            val className = file.getFileClassname(directory)
            WovenInfoUtils.addClassName(className)
            if (file.name.endsWith(Utils.AOP_CONFIG_END_NAME)) {
                FileInputStream(file).use { inputs ->
                    val classReader = ClassReader(inputs.readAllBytes())
                    classReader.accept(
                        SearchAOPConfigVisitor(), ClassReader.EXPAND_FRAMES
                    )
                }
            } else if (file.absolutePath.endsWith(Utils._CLASS)) {
                if (AndroidAopConfig.verifyLeafExtends && !className.startsWith("kotlinx/") && !className.startsWith(
                        "kotlin/"
                    )
                ) {
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
            }

        }
    }

    fun processJarForConfig(file: File) {
        WovenInfoUtils.addClassPath(file.absolutePath)
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            try {
                val entryName = jarEntry.name
                if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                    continue
                }
                if (entryName.endsWith(Utils._CLASS)) {
                    WovenInfoUtils.addClassName(entryName)
                }
//                    logger.error("entryName="+entryName)
                if (entryName.endsWith(Utils.AOP_CONFIG_END_NAME)) {
                    jarFile.getInputStream(jarEntry).use { inputs ->
                        val classReader = ClassReader(inputs.readAllBytes())
                        classReader.accept(
                            SearchAOPConfigVisitor(), ClassReader.EXPAND_FRAMES
                        )
                    }
                } else if (entryName.endsWith(Utils._CLASS)) {
                    if (AndroidAopConfig.verifyLeafExtends && !entryName.startsWith("kotlinx/") && !entryName.startsWith(
                            "kotlin/"
                        )
                    ) {
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

    fun searchJoinPointLocationStart(project: Project) {
        if (WovenInfoUtils.isHasExtendsReplace() && isAndroidModule) {
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
        deleteClassMethodRecords: MutableSet<String>
    ) {
        if (file.isFile) {
            val isClassFile = file.name.endsWith(Utils._CLASS)
            val entryName = file.getFileClassname(directory)
            val thisClassName = Utils.slashToDotClassName(entryName).replace(Utils._CLASS, "")
            val className = file.getFileClassname(directory).replace(".class", "")
            if (isClassFile && AndroidAopConfig.inRules(thisClassName)) {
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

                                        override fun onThrowOverrideMethod(className: String) {
                                            throwOverride(className)
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
                                if (e is AndroidAOPOverrideMethodException) {
                                    throw e
                                }
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            if (file.absolutePath.endsWith(Utils._CLASS)) {
                WovenInfoUtils.addExtendsReplace(slashToDot(className))

                val isAopCutClass =
                    WovenInfoUtils.isAopMethodCutClass(className) || WovenInfoUtils.isAopMatchCutClass(
                        className
                    )
                if (isAopCutClass) {
                    FileInputStream(file).use { inputs ->
                        val bytes = inputs.readAllBytes()
                        if (bytes.isNotEmpty()) {
                            val classReader = ClassReader(bytes)
                            classReader.accept(
                                SuspendReturnScanner(), 0
                            )
                        }
                    }
                }

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
                        if (methodName != "@null") {
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
                                                entryName.replace(Utils._CLASS, "")
                                            ),
                                            aopMatchCut.cutClassName,
                                            CutMethodJson(name, descriptor, false)
                                        )
                                        val methodRecord = MethodRecord(
                                            name,
                                            descriptor,
                                            mutableSetOf(aopMatchCut.cutClassName),
                                            false,
                                            mutableMapOf<String, CutInfo>().apply {
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
        deleteClassMethodRecords: MutableSet<String>
    ) {
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            try {
                val entryName = jarEntry.name
                if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    continue
                }
                val isClassFile = entryName.endsWith(Utils._CLASS)
//                    printLog("tranEntryName="+tranEntryName)
                val thisClassName = Utils.slashToDotClassName(entryName).replace(Utils._CLASS, "")
                val className = entryName.replace(".class", "")
                if (isClassFile && AndroidAopConfig.inRules(thisClassName)) {

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

                                            override fun onThrowOverrideMethod(className: String) {
                                                throwOverride(className)
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
                        }
                    }
                }
                if (entryName.endsWith(Utils._CLASS)) {

                    WovenInfoUtils.addExtendsReplace(slashToDot(className))

                    val isAopCutClass =
                        WovenInfoUtils.isAopMethodCutClass(className) || WovenInfoUtils.isAopMatchCutClass(
                            className
                        )
                    if (isAopCutClass) {
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            val bytes = inputs.readAllBytes()
                            if (bytes.isNotEmpty()) {
                                val classReader = ClassReader(bytes)
                                classReader.accept(
                                    SuspendReturnScanner(), 0
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is AndroidAOPOverrideMethodException) {
                    throw e
                }
                printLog("Merge jar error entry:[${jarEntry.name}], error message:$e")
            }
        }
        jarFile.close()
    }

    private fun throwOverride(className: String) {
        val hintFilePath = Utils.overrideClassFile(project, variantName)
        InitConfig.exportOverrideClassFile(File(hintFilePath), mutableListOf(className))
        throw AndroidAOPOverrideMethodException("重写$className 的相关方法已经改变，需要 clean 后重新编译")
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
        file: File
    ) {
        val jarFile = JarFile(file)
        val enumeration = jarFile.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            try {
                val entryName = jarEntry.name
                if (jarEntry.isDirectory || entryName.isEmpty() || entryName.startsWith("META-INF/") || "module-info.class" == entryName) {
                    continue
                }
                val realMethodsRecord: HashMap<String, MethodRecord>? = WovenInfoUtils.getClassMethodRecord(entryName)

                if (realMethodsRecord != null){
                    jarFile.getInputStream(jarEntry).use { inputs ->
                        WovenIntoCode.searchSuspendClass(inputs.readAllBytes(),realMethodsRecord)
                    }
                }
            } catch (e: Exception) {
                if (e is AndroidAOPOverrideMethodException) {
                    throw e
                }
                printLog("Merge jar error entry:[${jarEntry.name}], error message:$e")
            }
        }
        jarFile.close()
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

    fun wovenIntoCodeForReplace(byteArray: ByteArray): WovenResult {
        val cr = ClassReader(byteArray)
        val cw = ClassWriter(cr, 0)
        var thisHasCollect = false
        var thisHasStaticClock = false
        var thisCollectClassName: String? = null
        var replaceResult = false
        val cv = object : MethodReplaceInvokeVisitor(cw) {
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
            }
        }

        cr.accept(cv, 0)

        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock) {
                WovenIntoCode.wovenStaticCode(cw, it)
                replaceResult = true
            }
        }
        return WovenResult(cw.toByteArray(), replaceResult)
    }

    fun wovenIntoCodeForExtendsClass(byteArray: ByteArray): WovenResult {
        val cr = ClassReader(byteArray)
        val cw = ClassWriter(cr, 0)
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

        cr.accept(cv, 0)

        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock) {
                WovenIntoCode.wovenStaticCode(cw, it)
                replaceResult = true
            }
        }

        return WovenResult(cw.toByteArray(), replaceResult)
    }
}