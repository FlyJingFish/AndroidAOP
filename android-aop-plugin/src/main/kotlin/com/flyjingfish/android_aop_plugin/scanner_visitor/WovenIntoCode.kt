package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.CutFileJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.AppClasses
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassNameToConversions
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.KeywordChecker
import com.flyjingfish.android_aop_plugin.utils.RuntimeProject
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.CONVERSIONS_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.JOIN_LOCK
import com.flyjingfish.android_aop_plugin.utils.Utils.JOIN_POINT_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.KEEP_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.dotToSlash
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import com.flyjingfish.android_aop_plugin.utils.addPublic
import com.flyjingfish.android_aop_plugin.utils.checkExist
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.instanceof
import com.flyjingfish.android_aop_plugin.utils.isHasMethodBody
import com.flyjingfish.android_aop_plugin.utils.isStaticMethod
import com.flyjingfish.android_aop_plugin.utils.printDetail
import com.flyjingfish.android_aop_plugin.utils.printLog
import com.flyjingfish.android_aop_plugin.utils.saveFile
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import kotlinx.metadata.isNullable
import kotlinx.metadata.isSuspend
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.Metadata
import kotlinx.metadata.jvm.signature
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodNode
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.regex.Matcher
import java.util.regex.Pattern


object WovenIntoCode {
    @Throws(Exception::class)
    fun modifyClass(
        project: RuntimeProject,
        inputStreamBytes: ByteArray?,
        methodRecordHashMap: HashMap<String, MethodRecord>,
        hasReplace:Boolean,
        invokeStaticClass:String,
        wovenClassWriterFlags:Int, wovenParsingOptions:Int,
        isSuspendClass:Boolean
    ): ByteArray {
        val wovenRecord = mutableListOf<MethodRecord>()
        val overrideRecord = mutableListOf<MethodRecord>()
        var suspendClassReturnClassName :String ?= null

        val cr = ClassReader(inputStreamBytes)
        val cw = FixBugClassWriter(cr, wovenClassWriterFlags)
        val returnTypeMap = mutableMapOf<String,String?>()
        val isModifyPublic = ClassFileUtils.reflectInvokeMethod && ClassFileUtils.reflectInvokeMethodStatic

        fun visitMethod4Record(access: Int,
                               name: String,
                               descriptor: String,
                               signature: String?,
                               exceptions: Array<String?>?,
                               className:String ){
            methodRecordHashMap.forEach { (_: String, value: MethodRecord) ->
                val oldMethodName = value.methodName
                val oldDescriptor = value.descriptor
                val newMethodName = Utils.getTargetMethodName(oldMethodName, className, descriptor)
                if (newMethodName == name && oldDescriptor == descriptor){
                    wovenRecord.add(value)
//                    WovenInfoUtils.addAopMethodCutInnerClassInfoInvokeMethod(className,newMethodName,descriptor)
                }
                if (value.overrideMethod && oldMethodName == name && oldDescriptor == descriptor){
                    overrideRecord.add(value)
                }
            }


            if (signature != null && descriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")) {
                val returnTypeKey = name+descriptor
                val returnTypeClassName :String? = Utils.getSuspendMethodType(signature)
                returnTypeMap[returnTypeKey] = returnTypeClassName
//                printLog("signature=$signature")
            }
        }
        var thisHasCollect = false
        var thisHasStaticClock = false
        var thisCollectClassName :String ?= null
        var ctClazzName :String ?= null
        var superClassName :String ?= null
        val deleteNews = mutableMapOf<String,List<ReplaceMethodInfo>>()
        if (hasReplace){
            cr.accept(object :MethodReplaceInvokeVisitor(cw,wovenClassWriterFlags,wovenParsingOptions){
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access.addPublic(isModifyPublic), name, signature, superName, interfaces)
                    thisHasCollect = hasCollect
                    thisCollectClassName = thisClassName
                    superClassName = modifyExtendsClassName ?: superName
                    ctClazzName = name
                }
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String?>?
                ): MethodVisitor? {
                    visitMethod4Record(access, name, descriptor, signature, exceptions, className)
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
                    deleteNews.putAll(mDeleteNews)
                }
            }, wovenParsingOptions)
        }else{
            cr.accept(object : ReplaceBaseClassVisitor(cw) {
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access.addPublic(isModifyPublic), name, signature, superName, interfaces)
                    thisHasCollect = hasCollect
                    thisCollectClassName = thisClassName
                    superClassName = if (modifyExtendsClassName != null){
                        modifyExtendsClassName
                    }else{
                        superName
                    }
                    ctClazzName = name
                }
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String?>?
                ): MethodVisitor? {
                    visitMethod4Record(access, name, descriptor, signature, exceptions, clazzName)
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
            }, wovenParsingOptions)
        }
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            if (value in wovenRecord){
                return@forEach
            }
            if (value.overrideMethod){
                if (value !in overrideRecord){
                    return@forEach
                }
            }
            val oldMethodName = value.methodName
//            val targetMethodName = oldMethodName + METHOD_SUFFIX
            val oldDescriptor = value.descriptor
            cr.accept(object : ReplaceBaseClassVisitor(cw) {
                lateinit var className: String
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access.addPublic(isModifyPublic), name, signature, superName, interfaces)
                    if (isSuspendClass){
                        suspendClassReturnClassName = Utils.getSuspendClassType(signature)
//                        printLog("wovenCode === $signature ==== $returnClassName")
                    }
//Lkotlin/coroutines/jvm/internal/SuspendLambda;Lkotlin/jvm/functions/Function2<Lkotlinx/coroutines/CoroutineScope;Lkotlin/coroutines/Continuation<-Ljava/lang/Integer;>;Ljava/lang/Object;>;
                    className = name
                }
                override fun visitAnnotation(
                    descriptor: String?,
                    visible: Boolean
                ): AnnotationVisitor {
                    return object :
                        AnnotationVisitor(Opcodes.ASM9) {
                        override fun visitAnnotation(
                            name: String?,
                            descriptor: String?
                        ): AnnotationVisitor? {
                            return super.visitAnnotation(name, descriptor)
                        }
                    }
                }

                override fun visitModule(
                    name: String?,
                    access: Int,
                    version: String?
                ): ModuleVisitor? {
                    return null
                }

                override fun visitTypeAnnotation(
                    typeRef: Int,
                    typePath: TypePath?,
                    descriptor: String?,
                    visible: Boolean
                ): AnnotationVisitor? {
                    return null
                }


                override fun visitRecordComponent(
                    name: String?,
                    descriptor: String?,
                    signature: String?
                ): RecordComponentVisitor? {
                    return null
                }

                override fun visitField(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    value: Any?
                ): FieldVisitor? {
                    return null
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String?>?
                ): MethodVisitor? {
                    return if (oldMethodName == name && oldDescriptor == descriptor) {
                        val newAccess = if (access.isStaticMethod()){
                            ACC_PUBLIC + ACC_STATIC + ACC_FINAL
                        }else{
                            ACC_PUBLIC + ACC_FINAL
                        }
                        val newMethodName = Utils.getTargetMethodName(oldMethodName, className, descriptor)
                        var mv: MethodVisitor? = super.visitMethod(
                            newAccess,
                            newMethodName,
                            descriptor,
                            signature,
                            exceptions
                        )

                        if (hasReplace && mv != null && access.isHasMethodBody()) {
                            mv = if (wovenParsingOptions != 0){
                                MethodReplaceInvokeAdapter2(className,oldSuperName,access,name,descriptor,mv).apply {
                                    utils.onResultListener = object : MethodReplaceInvokeAdapterUtils.OnResultListener{
                                        override fun onBack() {

                                        }

                                        override fun onBack(delNews:List<ReplaceMethodInfo>) {
                                            if (delNews.isNotEmpty()){
                                                deleteNews["$newMethodName@$descriptor"] = delNews
                                            }
                                        }
                                    }
                                }
                            }else{
                                MethodReplaceInvokeAdapter(className,oldSuperName,access,name,descriptor,mv)
                            }
                        }
//                        WovenInfoUtils.addAopMethodCutInnerClassInfoInvokeMethod(className,newMethodName,descriptor)
                        RemoveAnnotation(mv)
                    } else {
                        null
                    }
                }

                override fun visitEnd() {
                    super.visitEnd()
                }
            }, wovenParsingOptions)
        }
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            if (value in wovenRecord){
                return@forEach
            }
            if (value in overrideRecord){
                return@forEach
            }
            if (value.overrideMethod && superClassName != null && ctClazzName != null){
                val ctClass = try {
                    ClassPoolUtils.classPool?.getCtClass(value.overrideClassname) ?: return@forEach
                } catch (e: Exception) {
                    return@forEach
                }
                try {
                    val ctMethod = ctClass.getMethod(value.methodName,value.descriptor)
                    val methodParamNamesScanner = MethodParamNamesScanner(ctClass.toBytecode())
                    val ctClasses = ctMethod.parameterTypes
                    val len = ctClasses.size
                    val argInfos = methodParamNamesScanner.getParamInfo(
                        value.methodName,
                        value.descriptor,
                        len
                    )
                    val methodNode = methodParamNamesScanner.getMethodNode(value.methodName, value.descriptor)
                    wovenMethodCode(cw,superClassName!!, value.methodName,value.methodName,value.descriptor,ctMethod.modifiers,methodNode,argInfos)
                    val newMethodName = Utils.getTargetMethodName(value.methodName, ctClazzName!!, value.descriptor)
                    wovenMethodCode(cw,superClassName!!, value.methodName,newMethodName,value.descriptor,ACC_PUBLIC + ACC_FINAL,methodNode,argInfos)
                    WovenInfoUtils.recordOverrideClassname(value.overrideClassname,value.methodName, value.descriptor)
                } catch (_: Exception) {
                }
//                ctClass.detach()
            }
        }
        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock){
                wovenStaticCode(cw, it)
            }
        }

        val cp = ClassPoolUtils.getNewClassPool()
//        val cp = ClassPool.getDefault();
        val newClassByte = deleteNews(cw.toByteArray(),deleteNews,wovenClassWriterFlags,wovenParsingOptions)
        val byteArrayInputStream: InputStream =
            ByteArrayInputStream(newClassByte)
        val ctClass = cp.makeClass(byteArrayInputStream)
        val metadata = extractMetadataFromByteArray(newClassByte)
        val kmFuntions = parseSuspendFunctions(metadata)
        cp.importPackage(JOIN_POINT_CLASS)
        cp.importPackage(CONVERSIONS_CLASS)
        cp.importPackage(KEEP_CLASS)
        cp.importPackage(JOIN_LOCK)
//        cp.importPackage(OBJECT_UTILS)
        val methodParamNamesScanner = MethodParamNamesScanner(newClassByte)

        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            val targetClassName = ctClass.name
            val oldMethodName = value.methodName
            val oldDescriptor = value.descriptor
            val targetMethodName = Utils.getTargetMethodName(oldMethodName,targetClassName,oldDescriptor)
            val targetFieldName = Utils.getTargetFieldName(oldMethodName,targetClassName,oldDescriptor)
            val cutClassNameArray = StringBuilder()
            value.cutClassName.toList().forEachIndexed { index, item ->
                cutClassNameArray.append("\"").append(item).append("\"")
                if (index != value.cutClassName.size - 1) {
                    cutClassNameArray.append(",")
                }
            }
            val invokeClassName = "${targetClassName}\$Invoke${(targetMethodName+oldDescriptor).computeMD5()}"
            val realInvokeClassNameReal = if (ClassFileUtils.reflectInvokeMethodStatic){
                invokeStaticClass
            }else{
                invokeClassName
            }
//            if (value in wovenRecord){
////                WovenInfoUtils.checkNoneInvokeClass(invokeClassName)
//                return@forEach
//            }
            var newMethodBody : String ?= null
            try {
                val ctMethod =
                    getCtMethod(ctClass, oldMethodName, oldDescriptor)
                val targetMethod =
                    getCtMethod(ctClass, targetMethodName, oldDescriptor)
                if (ctMethod == null){
                    if (!isSuspendClass){
                        printLog("------ctMethod ${targetClassName}${oldMethodName}${oldDescriptor} 方法找不到了-----")
                    }
                    return@forEach
                }else if (targetMethod == null){
                    if (!isSuspendClass){
                        printLog("------targetMethod ${targetClassName}${targetMethodName}${oldDescriptor} 方法找不到了-----")
                    }
                    return@forEach
                }

                val constPool = ctClass.classFile.constPool


                //给原有方法增加 @Keep，防止被混淆
                targetMethod.addKeepClassAnnotation(constPool)
                ctMethod.addKeepClassAnnotation(constPool)

                val isStaticMethod =
                    Modifier.isStatic(ctMethod.modifiers)
                val ctClasses = ctMethod.parameterTypes
                val len = ctClasses.size

                val targetField = getCtField(ctClass,targetFieldName)
                if (targetField == null){
                    if (isStaticMethod){
                        val extraField = CtField(cp.get(JOIN_LOCK), targetFieldName, ctClass)
                        extraField.modifiers = Modifier.PRIVATE or Modifier.FINAL or Modifier.STATIC
                        ctClass.addField(extraField, CtField.Initializer.byExpr("new $JOIN_LOCK()"));
                    }else{
                        val extraField = CtField(cp.get(JOIN_POINT_CLASS), targetFieldName, ctClass)
                        extraField.modifiers = Modifier.PRIVATE or Modifier.VOLATILE
                        ctClass.addField(extraField)
                    }

                }

                val argNameList = methodParamNamesScanner.getParamNames(
                    targetMethodName,
                    oldDescriptor,
                    len
                )
//                printLog("oldMethodName=$oldMethodName,oldDescriptor=$oldDescriptor,nameList=$argNameList")
                val paramsNamesBuffer = StringBuffer()
                val sortedMapSize = argNameList.size
                for ((paramNameIndex, argName) in argNameList.withIndex()) {
                    paramsNamesBuffer.append("\"").append(argName).append("\"")
                    if (paramNameIndex != sortedMapSize - 1) {
                        paramsNamesBuffer.append(",")
                    }
                }

//                printLog("targetClassName=$targetClassName,oldMethodName=$oldMethodName,oldDescriptor=$oldDescriptor,paramNames=$paramNamesMap")
                val argsBuffer = StringBuffer()

                val paramsClassNamesBuffer = StringBuffer()
                val paramsClassesBuffer = StringBuffer()

                val returnTypeName = ctMethod.returnType.name

                // 非静态的成员函数的第一个参数是this
                val pos =  1
                val isHasArgs = len > 0
                val invokeBuffer = StringBuffer()
                var argClassHasKeyword = false
                for (i in ctClasses.indices) {
                    val name = ctClasses[i].name

                    paramsClassNamesBuffer.append("\"").append(name).append("\"")
                    paramsClassesBuffer.append(ClassNameToConversions.string2Class(name))

                    val index = i + pos

                    argsBuffer.append(String.format(ClassNameToConversions.getArgsXObject(name), "\$"+index))
                    invokeBuffer.append(String.format(ClassNameToConversions.getInvokeXObject(name), "\$2[$i]"))
                    if (KeywordChecker.containsKeywordAsWord(name)){
                        argClassHasKeyword = true
                    }
                    if (i != len - 1) {
                        paramsClassNamesBuffer.append(",")
                        paramsClassesBuffer.append(",")
                        argsBuffer.append(",")
                        invokeBuffer.append(",")
                    }
                }


                val suspendMethod = if (ctClasses.isNotEmpty()){
                    returnTypeName == "java.lang.Object" && ctClasses[ctClasses.size-1].name == "kotlin.coroutines.Continuation"
                }else{
                    false
                }
                val runtimeReturnTypeClassName = if (suspendMethod) {
                    val returnTypeClassName = returnTypeMap[oldMethodName + oldDescriptor]
                        ?: returnTypeMap[targetMethodName + oldDescriptor]
                    if (metadata != null) {
                        val suspendType = getFunctions(kmFuntions, "$oldMethodName$oldDescriptor")
                        if (suspendType == null) {
                            ClassPoolUtils.extractRawTypeName(cp,returnTypeClassName)?: returnTypeName
                        } else {
                            val type = ClassPoolUtils.extractRawTypeNames(cp,suspendType)
                            if (type != null && type.first) {
                                type.second
                            } else {
                                ClassPoolUtils.extractRawTypeName(cp,returnTypeClassName)?: returnTypeName
                            }
                        }
                    } else {
                        ClassPoolUtils.extractRawTypeName(cp,returnTypeClassName)?: returnTypeName
                    }
                }else{
                    returnTypeName
                }
                val argsStr =if (isHasArgs) "new Object[]{$argsBuffer}" else "null"
                val returnStr = if (isSuspendClass){
                    suspendClassReturnClassName = ClassPoolUtils.extractRawTypeName(cp,suspendClassReturnClassName)
                    String.format(
                        ClassNameToConversions.getReturnXObject(returnTypeName), "pointCut.joinPointReturnExecute($argsStr,${if (suspendClassReturnClassName == null) null else KeywordChecker.getClass(suspendClassReturnClassName)})"
                    )
                }else{
                    String.format(
                        ClassNameToConversions.getReturnXObject(returnTypeName), "pointCut.joinPointExecute($argsStr,${if (suspendMethod) "(kotlin.coroutines.Continuation)\$$len" else "null" })"
                    )
                }

//                val returnStr2 = if (returnTypeName == "void"){
//                    "$returnStr;\nreturn;"
//                }else{
//                    "$returnStr;\n"
//                }

                val invokeReturnStr:String? = ClassNameToConversions.getReturnInvokeXObject(returnTypeName)
                val invokeStr =if (isStaticMethod){
                    "$targetClassName.$targetMethodName($invokeBuffer)"
                }else{
                    "(($targetClassName)\$1).$targetMethodName($invokeBuffer)"
                }
                val invokeBody = if (invokeReturnStr == null){
                    "{$invokeStr;"+
                            "        return null;}"
                }else{

                    "{${String.format(invokeReturnStr,invokeStr)};"+
                            "        return returnValue;}"
                }
                val isCreateInvokeClass = !KeywordChecker.containsKeywordAsWord(targetClassName) && !argClassHasKeyword
                if (isCreateInvokeClass){
                    ClassFileUtils.get(project).createInvokeClass(invokeStaticClass,invokeClassName,invokeBody, oldMethodName + oldDescriptor)
                }
                ClassFileUtils.get(project).outputCacheDir?.let {
                    cp.appendClassPath(it.absolutePath)
                }
                cp.importPackage(realInvokeClassNameReal)
//                val constructor = "\"${invokeClassName.replace(".", "_")}\",$targetClassName.class,${if(isStaticMethod)"null" else "\$0"},\"$oldMethodName\",\"$targetMethodName\",${value.lambda}"
                val constructor = "\"${invokeClassName.replace(".", "_")}\",${KeywordChecker.getClass(targetClassName)},\"$oldMethodName\",\"$targetMethodName\",${value.lambda},${if(isStaticMethod)"null" else "\$0"}"
//                val setArgsStr =
//                    (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
//                        (if (isHasArgs) "        pointCut.setArgs(args);\n" else "        pointCut.setArgs(null);\n")

                val invokeMethodStr = if (!isCreateInvokeClass){
                    "null"
                }else{
                    if (ClassFileUtils.reflectInvokeMethod){
                        if (ClassFileUtils.reflectInvokeMethodStatic){
                            KeywordChecker.getInstance(invokeStaticClass,InvokeMethod::class.java.name)
                        }else{
                            "null"
                        }
                    }else{
                        KeywordChecker.getInstance(invokeClassName,InvokeMethod::class.java.name)
                    }
                }

                val synchronizedObject = if (isStaticMethod){
                    "$targetClassName.class"
                }else {
                    "this"
                }

                val initBody = "AndroidAopJoinPoint pointCut = new AndroidAopJoinPoint($constructor);\n" +
                                "String[] cutClassNames = new String[]{$cutClassNameArray};\n"+
                                "pointCut.setCutMatchClassNames(cutClassNames);\n"+
                                "Class[] classes = new Class[]{$paramsClassesBuffer};\n"+
                                "pointCut.setArgClasses(classes);\n"+
                                "String[] paramNames = new String[]{$paramsNamesBuffer};\n"+
                                "pointCut.setParamNames(paramNames);\n"+
                                "pointCut.setReturnClass(${KeywordChecker.getClass(runtimeReturnTypeClassName)});\n"+
                                "pointCut.setInvokeMethod($invokeMethodStr,$suspendMethod);\n"

                newMethodBody = if (isStaticMethod){
                    " { " +
                            "if ($targetFieldName.getJoinPoint() == null) {\n" +
                            "        synchronized ($targetFieldName) {\n" +
                            "            if ($targetFieldName.getJoinPoint() == null) {\n" +
                                            initBody+
                            "                $targetFieldName.setJoinPoint(pointCut);\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            " AndroidAopJoinPoint pointCut = $targetFieldName.getJoinPoint();\n"+
                            "        "+returnStr+";}"
                }else{
                    " { " +
                            "if ($targetFieldName == null) {\n" +
                            "        synchronized ($synchronizedObject) {\n" +
                            "            if ($targetFieldName == null) {\n" +
                                            initBody+
                            "                $targetFieldName = pointCut;\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            " AndroidAopJoinPoint pointCut = $targetFieldName;\n"+
                            "        "+returnStr+";}"
                }

                ctMethod.setBody(newMethodBody)
                InitConfig.putCutInfo(value)
            } catch (e: Exception) {
                printLog("newMethodBody=$newMethodBody")
                if (e is NotFoundException || e is CannotCompileException){
                    WovenInfoUtils.deleteAopMethodCutInnerClassInfoInvokeMethod(Utils.dotToSlash(targetClassName),targetMethodName,oldDescriptor)
                    ClassFileUtils.get(project).deleteInvokeClass(invokeClassName)
                    e.printDetail()
                }else{
                    throw e
                }
            }
        }
        val wovenBytes = ctClass.toBytecode()
//        ctClass.detach()
        return wovenBytes
    }


    // 提取 @Metadata 注解
    private fun extractMetadataFromByteArray(byteArray: ByteArray): Metadata? {
        val classReader = ClassReader(byteArray)
        var metadata: Metadata? = null

        classReader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                if (desc == "Lkotlin/Metadata;") {
                    return object : AnnotationVisitor(Opcodes.ASM9) {
                        var kind = 1
                        var mv: IntArray? = null
                        var d1: Array<String>? = null
                        var d2: Array<String>? = null
                        var xs: String? = null
                        var pn: String? = null
                        var xi = 0

                        override fun visit(name: String, value: Any?) {
                            when (name) {
                                "k" -> kind = value as Int
                                "xs" -> xs = value as String
                                "pn" -> pn = value as String
                                "xi" -> xi = value as Int
                            }
                        }

                        override fun visitArray(name: String): AnnotationVisitor {
                            return object : AnnotationVisitor(Opcodes.ASM9) {
                                val list = mutableListOf<String>()
                                override fun visit(name: String?, value: Any?) {
                                    if (value is String) list.add(value)
                                }

                                override fun visitEnd() {
                                    when (name) {
                                        "d1" -> d1 = list.toTypedArray()
                                        "d2" -> d2 = list.toTypedArray()
                                    }
                                }
                            }
                        }

                        override fun visitEnd() {
                            if (d1 != null && d2 != null && mv != null) {
                                metadata = Metadata(kind, mv, d1!!, d2!!, xs, pn, xi)
                            } else if (d1 != null && d2 != null) {
                                metadata = Metadata(kind, intArrayOf(1, 8, 0), d1!!, d2!!, xs, pn, xi)
                            }
                        }
                    }
                }
                return null
            }
        }, 0)

        return metadata
    }

    // 使用 kotlinx-metadata 解析 suspend 方法及返回类型
    private fun getFunctions(functions:List<KmFunction>?,signature:String):String? {
        if (functions == null) return null
        for (func in functions) {
            if (func.isSuspend && signature == func.signature.toString()) {
                return func.returnType.toJavaType()
            }
        }
        return null
    }

    private fun parseSuspendFunctions(metadata: Metadata?):MutableList<KmFunction>? {
        if (metadata == null) return null
        val km = KotlinClassMetadata.readStrict(metadata)
        val kmClass = (km as? KotlinClassMetadata.Class)?.kmClass ?: return null
        return kmClass.functions
    }

    private fun KmType.toJavaType(isArray:Boolean = false): String? {
        val baseType = when (val classifier = this.classifier) {
            is KmClassifier.Class -> classifier.name.replace('.', '$').replace('/', '.')
            is KmClassifier.TypeAlias -> classifier.name.replace('.', '$').replace('/', '.')
            else -> null
        }

        val mappedBase = when (baseType) {
            // 基本类型
            "kotlin.Int" -> {
                if (isNullable || isArray){
                    "java.lang.Integer"
                }else{
                    "int"
                }
            }
            "kotlin.Long" -> {
                if (isNullable || isArray){
                    "java.lang.Long"
                }else{
                    "long"
                }
            }
            "kotlin.Short" -> {
                if (isNullable || isArray){
                    "java.lang.Short"
                }else{
                    "short"
                }
            }
            "kotlin.Byte" -> {
                if (isNullable || isArray){
                    "java.lang.Byte"
                }else{
                    "byte"
                }
            }
            "kotlin.Boolean" -> {
                if (isNullable || isArray){
                    "java.lang.Boolean"
                }else{
                    "boolean"
                }
            }
            "kotlin.Char" -> {
                if (isNullable || isArray){
                    "java.lang.Character"
                }else{
                    "char"
                }
            }
            "kotlin.Float" -> {
                if (isNullable || isArray){
                    "java.lang.Float"
                }else{
                    "float"
                }
            }
            "kotlin.Double" -> {
                if (isNullable || isArray){
                    "java.lang.Double"
                }else{
                    "double"
                }
            }

            // 常用对象类型
            "kotlin.Unit" -> {
                if (isNullable || isArray){
                    "kotlin.Unit"
                }else{
                    "void"
                }
            }
            "kotlin.String" -> "java.lang.String"
            "kotlin.Any" -> "java.lang.Object"
            "kotlin.Nothing" -> "java.lang.Void"
            "kotlin.Throwable" -> "java.lang.Throwable"

            // Java 集合
            "kotlin.collections.List", "kotlin.collections.MutableList" -> "java.util.List"
            "kotlin.collections.Set", "kotlin.collections.MutableSet" -> "java.util.Set"
            "kotlin.collections.Map", "kotlin.collections.MutableMap" -> "java.util.Map"

            // 原始数组类型（非泛型）
            "kotlin.IntArray" -> "int[]"
            "kotlin.LongArray" -> "long[]"
            "kotlin.ShortArray" -> "short[]"
            "kotlin.ByteArray" -> "byte[]"
            "kotlin.BooleanArray" -> "boolean[]"
            "kotlin.CharArray" -> "char[]"
            "kotlin.FloatArray" -> "float[]"
            "kotlin.DoubleArray" -> "double[]"
            // 泛型数组，如 Array<String> → java.lang.String[]
            "kotlin.Array" -> {
                val elementType = arguments.firstOrNull()?.type?.toJavaType(true) ?: "java.lang.Object"
                "$elementType[]"
            }
            else -> {
                if (baseType?.startsWith("kotlin.") == true){
                    null
                }else{
                    baseType
                }
            } // 可能是自定义类
        }
        return mappedBase
    }
    fun deleteNews(classByte:ByteArray,deleteNews : MutableMap<String,List<ReplaceMethodInfo>>,wovenClassWriterFlags:Int,wovenParsingOptions:Int):ByteArray{
        return if (deleteNews.isNotEmpty()){
            try {
                val cr = ClassReader(classByte)
                val cw = FixBugClassWriter(cr, wovenClassWriterFlags)
                val cv = object : ClassVisitor(Opcodes.ASM9, cw) {
                    lateinit var className:String
                    lateinit var _superClassName:String
                    override fun visit(
                        version: Int,
                        access: Int,
                        name: String,
                        signature: String?,
                        superName: String,
                        interfaces: Array<out String>?
                    ) {
                        className = name
                        _superClassName = superName
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
                        val list = deleteNews["$name@$descriptor"]
                        return if (!list.isNullOrEmpty()){
                            MethodReplaceInvokeInitAdapter(className,_superClassName,access,name,descriptor,signature,exceptions,mv,list)
                        }else{
                            mv
                        }
                    }
                }
                cr.accept(cv, wovenParsingOptions)
                cw.toByteArray()
            } catch (e: Exception) {
                e.printDetail()
                classByte
            }
        }else{
            classByte
        }
    }


    fun searchSuspendClass(
        inputStreamBytes: ByteArray?,
        methodRecordHashMap: HashMap<String, MethodRecord>,
    ) {
        val cr = ClassReader(inputStreamBytes)

        fun visitMethod4Record(access: Int,
                               name: String,
                               descriptor: String,
                               signature: String?,
                               exceptions: Array<String?>?,
                               className:String ){
            methodRecordHashMap.forEach { (_: String, value: MethodRecord) ->
                val oldMethodName = value.methodName
                val oldDescriptor = value.descriptor
                val newMethodName = Utils.getTargetMethodName(oldMethodName, className, descriptor)

                WovenInfoUtils.addAopMethodCutInnerClassInfoInvokeMethod(className,newMethodName,descriptor)
            }


        }
        cr.accept(object : ClassVisitor(ASM9) {
            lateinit var clazzName:String
            override fun visit(
                version: Int,
                access: Int,
                name: String,
                signature: String?,
                superName: String,
                interfaces: Array<out String>?
            ) {
                super.visit(version, access, name, signature, superName, interfaces)
                clazzName = name
            }
            override fun visitMethod(
                access: Int,
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<String?>?
            ): MethodVisitor? {
                visitMethod4Record(access, name, descriptor, signature, exceptions, clazzName)
                val mv = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                )
                return mv
            }
        }, ClassReader.EXPAND_FRAMES)
    }

    private fun CtMethod.addKeepClassAnnotation(constPool: ConstPool){
        val visibleTagAttribute :AttributeInfo? = methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
        val annotationsAttribute = if (visibleTagAttribute == null){
            AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag)
        }else{
            visibleTagAttribute as AnnotationsAttribute
        }
        val ctMethodNoneHasKeep = annotationsAttribute.getAnnotation(KEEP_CLASS) == null
        if (ctMethodNoneHasKeep){
            val annotation = Annotation(KEEP_CLASS, constPool)

            // 给注解添加参数 keepName = 方法名
            val methodName = this.name
            annotation.addMemberValue(
                "keepName",
                StringMemberValue(methodName, constPool)
            )

            annotationsAttribute.addAnnotation(annotation)
            if (visibleTagAttribute == null){
                methodInfo.addAttribute(annotationsAttribute)
            }
        }
    }

    private fun CtMethod.removeAllAnnotation(){
        methodInfo.removeAttribute(AnnotationsAttribute.visibleTag)
    }

    fun wovenStaticCode(cw:ClassWriter,thisClassName:String){
        val mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null)
        for (moduleName in AppClasses.getAllModuleNames()) {
            val tryStart = Label()
            val tryEnd = Label()
            val labelCatch = Label()
            val tryCatchBlockEnd = Label()

            mv.visitTryCatchBlock(
                tryStart,
                tryEnd,
                labelCatch,
                "java/lang/NoClassDefFoundError"
            )
            mv.visitLabel(tryStart)

            val className1 = "$thisClassName\$Inner${thisClassName.computeMD5()}_${moduleName.computeMD5()}"
            mv.visitTypeInsn(AdviceAdapter.NEW,Utils.dotToSlash(className1))
            mv.visitInsn(AdviceAdapter.DUP)
            mv.visitMethodInsn(AdviceAdapter.INVOKESPECIAL,Utils.dotToSlash(className1),"<init>","()V",false)
            mv.visitInsn(POP) // 保证 try 分支栈深为 0

            mv.visitLabel(tryEnd)
            mv.visitJumpInsn(Opcodes.GOTO, tryCatchBlockEnd)

            mv.visitLabel(labelCatch)
            mv.visitVarInsn(Opcodes.ASTORE, 0)

            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/NoClassDefFoundError",
                "printStackTrace",
                "()V",
                false
            )

            mv.visitLabel(tryCatchBlockEnd)


        }

        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
    }
    private fun wovenMethodCode(cw:ClassWriter, superClassName:String, superMethodName:String, methodName:String, methodDescriptor:String, methodAccess:Int,methodNode:MethodNode? ,argInfos:List<LocalVariableNode>){
        val mv = cw.visitMethod(methodAccess, methodName, methodDescriptor, methodNode?.signature, methodNode?.exceptions?.toTypedArray())
        val isVoid = Type.getReturnType(methodDescriptor).className == "void"
        val argTypes = Type.getArgumentTypes(methodDescriptor)
        mv.visitCode()

        if (argInfos.isNotEmpty()){

            for (info in argInfos) {
                val start = info.start.label
                val end = info.end.label
                mv.visitLabel(start)
                mv.visitLabel(end)
                mv.visitLocalVariable(info.name, info.desc, info.signature, start, start, info.index)
            }
        }

        mv.visitVarInsn(ALOAD, 0) // 加载 `this` 引用到操作数栈
        var localVarIndex = 1
        var maxStack = 1
        for (argType in argTypes) {
            maxStack += when (argType.sort) {
                Type.LONG ,Type.DOUBLE -> {
                    2
                }
                else -> {
                    1
                }
            }
            when (argType.sort) {
                Type.INT -> mv.visitVarInsn(ILOAD, localVarIndex) // 加载 int 类型参数
                Type.LONG -> mv.visitVarInsn(LLOAD, localVarIndex) // 加载 long 类型参数
                Type.FLOAT -> mv.visitVarInsn(FLOAD, localVarIndex) // 加载 float 类型参数
                Type.DOUBLE -> mv.visitVarInsn(DLOAD, localVarIndex) // 加载 double 类型参数
                Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT -> mv.visitVarInsn(
                    ILOAD,
                    localVarIndex
                )
                Type.OBJECT, Type.ARRAY -> mv.visitVarInsn(
                    ALOAD,
                    localVarIndex
                )
                else -> throw IllegalArgumentException("Unsupported parameter type: $argType")
            }
            localVarIndex += argType.size // 更新下一个局部变量的索引

        }

        mv.visitMethodInsn(
            INVOKESPECIAL,
            superClassName,
            superMethodName,
            methodDescriptor,
            false
        )
        if (isVoid) {
            mv.visitInsn(RETURN)
        } else {
            mv.visitInsn(IRETURN)
        }
        mv.visitMaxs(maxStack, localVarIndex)
        mv.visitEnd()

    }

    @Throws(NotFoundException::class)
    fun getCtMethod(ctClass: CtClass, methodName: String?, descriptor: String): CtMethod? {
        val ctMethods = ctClass.getDeclaredMethods(methodName)
        if (ctMethods != null && ctMethods.isNotEmpty()) {
            for (ctMethod in ctMethods) {
                val allSignature = ctMethod.signature
                if (descriptor == allSignature) {
                    return ctMethod
                }
            }
        }
        return null
    }

    fun getCtField(ctClass: CtClass, name: String): CtField? {
        return try {
            ctClass.getDeclaredField(name)
        } catch (e: NotFoundException) {
            null
        }
    }

    fun createInitClass(output:File) :File{
        val className = "com.flyjingfish.android_aop_annotation.utils.DebugAndroidAopInit"
        val cw = ClassWriter(COMPUTE_FRAMES or COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", null)

        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()


        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null)
        val map = WovenInfoUtils.getAopInstances()
        if (map.isNotEmpty()) {
            map.forEach { (key, value) ->
                RegisterMapWovenInfoCode.registerCreators(mv,key, value)
                RegisterMapWovenInfoCode.registerMatchCreators(mv,key, value)
            }
        }


        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        val path = output.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class"
        val classByteData = cw.toByteArray()
        val outFile = File(path)
        outFile.checkExist()
        classByteData.saveFile(outFile)
        return outFile
    }

    fun createCollectClass(output:File,moduleName: String) = runBlocking{
        val collectDirJobs = mutableListOf<Deferred<Unit>>()
        val classPool = ClassPoolUtils.getNewClassPool()
        WovenInfoUtils.getAopCollectClassMap().forEach {(key,value) ->
            if (value == null){
                return@forEach
            }
            val job = async(Dispatchers.IO) {
                val className = "$key\$Inner${key.computeMD5()}_${moduleName.computeMD5()}"
                val cw = ClassWriter(COMPUTE_FRAMES or COMPUTE_MAXS)
                cw.visit(V1_8, ACC_PUBLIC+ACC_STATIC, Utils.dotToSlash(className), null, "java/lang/Object", null)

                var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
                mv.visitVarInsn(ALOAD, 0)
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
                mv.visitInsn(RETURN)
                mv.visitMaxs(0, 0) //更新操作数栈
                mv.visitEnd()


                mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC, "<clinit>", "()V", null, null);
                val map: MutableMap<String,AopCollectClass> = value
                if (map.isNotEmpty()) {
                    val iterator = map.iterator();
                    while (iterator.hasNext()){
                        val aopCollectCut = iterator.next().value
                        val methodVisitor = mv
                        val collectExtendsClazz = Utils.dotToSlash(aopCollectCut.collectExtendsClassName)
                        val find = if (aopCollectCut.regex.isNotEmpty()){
                            val classnameArrayPattern: Pattern = Pattern.compile(aopCollectCut.regex)
                            val matcher: Matcher = classnameArrayPattern.matcher(
                                Utils.slashToDot(
                                    aopCollectCut.collectExtendsClassName
                                )
                            )
                            matcher.find()
                        }else{
                            true
                        }
                        if (find){
                            try {
                                val ctClass = classPool.get(WovenInfoUtils.getClassString(Utils.slashToDotClassName(aopCollectCut.collectExtendsClassName)))
                                val access = ctClass.modifiers
                                val isAbstractClass = access and ACC_ABSTRACT != 0
                                val isObject = Utils.slashToDot(aopCollectCut.collectClassName) == "java.lang.Object"
                                var isMatchExtends = false
                                val collectExtendsClassName = aopCollectCut.collectExtendsClassName
                                if (!isObject){
                                    var isDirectExtends = false
                                    var isImplementsInterface = false
                                    val interfaces = ctClass.interfaces
                                    if (interfaces != null) {
                                        for (subCtClass in ctClass.interfaces) {
                                            if (Utils.slashToDot(subCtClass.name) == aopCollectCut.collectClassName){
                                                isImplementsInterface = true
                                                break
                                            }
                                        }
                                    }

                                    if (isImplementsInterface || aopCollectCut.collectClassName == Utils.slashToDot(
                                            ctClass.superclass.name
                                        )
                                    ) {
                                        isDirectExtends = true
                                    }
                                    if (AopCollectCut.CollectType.DIRECT_EXTENDS.name == aopCollectCut.collectType) {
                                        if (isDirectExtends) {
                                            isMatchExtends = true
                                        }
                                    } else if (AopCollectCut.CollectType.LEAF_EXTENDS.name == aopCollectCut.collectType) {
                                        var isExtends = false
                                        if (isDirectExtends) {
                                            isExtends = true
                                        } else {
                                            val clsName = Utils.slashToDotClassName(collectExtendsClassName)
                                            val parentClsName = aopCollectCut.collectClassName
                                            if (clsName != Utils.slashToDotClassName(parentClsName)) {
                                                isExtends = clsName.instanceof(
                                                    Utils.slashToDotClassName(parentClsName)
                                                )
                                            }
                                        }
                                        if (isExtends && WovenInfoUtils.isLeaf(collectExtendsClassName)) {
                                            isMatchExtends = true
                                        }
                                    } else {
                                        if (isDirectExtends) {
                                            isMatchExtends = true
                                        } else {
                                            val clsName = Utils.slashToDotClassName(collectExtendsClassName)
                                            val parentClsName = aopCollectCut.collectClassName
                                            if (clsName != Utils.slashToDotClassName(parentClsName)) {
                                                val isInstanceof = clsName.instanceof(
                                                    Utils.slashToDotClassName(parentClsName)
                                                )
                                                if (isInstanceof) {
                                                    isMatchExtends = true
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    isMatchExtends = true
                                }
                                val isAdd = if (aopCollectCut.isClazz){
                                    true
                                }else !isAbstractClass

                                if (isMatchExtends && isAdd){
                                    val tryStart = Label()
                                    val tryEnd = Label()
                                    val labelCatch = Label()
                                    val tryCatchBlockEnd = Label()

                                    methodVisitor.visitTryCatchBlock(
                                        tryStart,
                                        tryEnd,
                                        labelCatch,
                                        "java/lang/IllegalAccessError"
                                    )
                                    methodVisitor.visitLabel(tryStart)
                                    if (aopCollectCut.isClazz){
                                        methodVisitor.visitLdcInsn(Type.getObjectType(collectExtendsClazz));
                                        val collectClazz = Utils.dotToSlash("java.lang.Class")
                                        methodVisitor.visitMethodInsn(
                                            AdviceAdapter.INVOKESTATIC,
                                            Utils.dotToSlash(aopCollectCut.invokeClassName),
                                            aopCollectCut.invokeMethod,
                                            "(L$collectClazz;)V",
                                            false
                                        )
                                    }else{
                                        methodVisitor.visitTypeInsn(AdviceAdapter.NEW, collectExtendsClazz)
                                        methodVisitor.visitInsn(AdviceAdapter.DUP)
                                        methodVisitor.visitMethodInsn(
                                            AdviceAdapter.INVOKESPECIAL,
                                            collectExtendsClazz,
                                            "<init>",
                                            "()V",
                                            false
                                        )
                                        val collectClazz = Utils.dotToSlash(aopCollectCut.collectClassName)
                                        methodVisitor.visitMethodInsn(
                                            AdviceAdapter.INVOKESTATIC,
                                            Utils.dotToSlash(aopCollectCut.invokeClassName),
                                            aopCollectCut.invokeMethod,
                                            "(L$collectClazz;)V",
                                            false
                                        )
                                    }
                                    methodVisitor.visitLabel(tryEnd)
                                    methodVisitor.visitJumpInsn(Opcodes.GOTO, tryCatchBlockEnd)

                                    methodVisitor.visitLabel(labelCatch)
                                    methodVisitor.visitVarInsn(Opcodes.ASTORE, 0)

                                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                                    methodVisitor.visitMethodInsn(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/IllegalAccessError",
                                        "printStackTrace",
                                        "()V",
                                        false
                                    )

                                    mv.visitLabel(tryCatchBlockEnd)
                                    InitConfig.addCollect(aopCollectCut)
                                }else{
                                    iterator.remove()
                                }
//                            ctClass.detach()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }


                mv.visitInsn(RETURN)
                mv.visitMaxs(0, 0)
                mv.visitEnd()
                val path = output.absolutePath + File.separatorChar +Utils.dotToSlash(className).adapterOSPath()+".class"
                val classByteData = cw.toByteArray()
                val outFile = File(path)
                outFile.checkExist()
                classByteData.saveFile(outFile)
            }
            collectDirJobs.add(job)
        }
        collectDirJobs.awaitAll()
    }

    fun deleteOtherCompileClass(project:RuntimeProject, variantName:String){
        val json : CutFileJson? = InitConfig.optFromJsonString(
            InitConfig.readAsString(Utils.aopCompileTempOtherJson(project,variantName)),
            CutFileJson::class.java)
        json?.let {
            it.cacheFileJson.forEach {filePath ->
                val file = File(filePath)
                if (file.exists()){
                    file.delete()
                }
            }
        }
    }
}
