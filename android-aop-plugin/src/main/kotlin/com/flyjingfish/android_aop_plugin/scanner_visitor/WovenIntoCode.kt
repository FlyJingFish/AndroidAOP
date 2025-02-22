package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.CutFileJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassNameToConversions
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.FileHashUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.CONVERSIONS_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.JOIN_POINT_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.KEEP_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.OBJECT_UTILS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.adapterOSPath
import com.flyjingfish.android_aop_plugin.utils.addPublic
import com.flyjingfish.android_aop_plugin.utils.checkExist
import com.flyjingfish.android_aop_plugin.utils.computeMD5
import com.flyjingfish.android_aop_plugin.utils.instanceof
import com.flyjingfish.android_aop_plugin.utils.isHasMethodBody
import com.flyjingfish.android_aop_plugin.utils.isStaticMethod
import com.flyjingfish.android_aop_plugin.utils.printLog
import com.flyjingfish.android_aop_plugin.utils.saveFile
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.Annotation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
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
        inputStreamBytes: ByteArray?,
        methodRecordHashMap: HashMap<String, MethodRecord>,
        hasReplace:Boolean,
        invokeStaticClass:String,
        isSuspend:Boolean = false
    ): ByteArray {
        val wovenRecord = mutableListOf<MethodRecord>()
        val overrideRecord = mutableListOf<MethodRecord>()
        var returnClassName :String ?= null

        val cr = ClassReader(inputStreamBytes)
        val cw = ClassWriter(cr, 0)
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
        if (hasReplace){
            cr.accept(object :MethodReplaceInvokeVisitor(cw){
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
            }, 0)
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
            }, 0)
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
                    if (isSuspend){
                        returnClassName = Utils.getSuspendClassType(signature)
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
                            mv = MethodReplaceInvokeAdapter(className,oldSuperName,name,descriptor,mv)
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
            }, 0)
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
        val newClassByte = cw.toByteArray()
        val byteArrayInputStream: InputStream =
            ByteArrayInputStream(newClassByte)
        val ctClass = cp.makeClass(byteArrayInputStream)
        cp.importPackage(JOIN_POINT_CLASS)
        cp.importPackage(CONVERSIONS_CLASS)
        cp.importPackage(KEEP_CLASS)
        cp.importPackage(OBJECT_UTILS)
        val methodParamNamesScanner = MethodParamNamesScanner(newClassByte)

        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            val targetClassName = ctClass.name
            val oldMethodName = value.methodName
            val oldDescriptor = value.descriptor
            val targetMethodName = Utils.getTargetMethodName(oldMethodName,targetClassName,oldDescriptor)
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
                    if (!isSuspend){
                        printLog("------ctMethod ${targetClassName}${oldMethodName}${oldDescriptor} 方法找不到了-----")
                    }
                    return@forEach
                }else if (targetMethod == null){
                    if (!isSuspend){
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
                val returnType = ctMethod.returnType

                // 非静态的成员函数的第一个参数是this
                val pos =  1
                val isHasArgs = len > 0
                val invokeBuffer = StringBuffer()
                for (i in ctClasses.indices) {
                    val aClass = ctClasses[i]
                    val name = aClass.name

                    paramsClassNamesBuffer.append("\"").append(name).append("\"")
                    paramsClassesBuffer.append(ClassNameToConversions.string2Class(name))

                    val index = i + pos

                    argsBuffer.append(String.format(ClassNameToConversions.getArgsXObject(name), "\$"+index))
                    invokeBuffer.append(String.format(ClassNameToConversions.getInvokeXObject(name), "\$2[$i]"))

                    if (i != len - 1) {
                        paramsClassNamesBuffer.append(",")
                        paramsClassesBuffer.append(",")
                        argsBuffer.append(",")
                        invokeBuffer.append(",")
                    }
                }


                val suspendMethod = if (ctClasses.isNotEmpty()){
                    returnType.name == "java.lang.Object" && ctClasses[ctClasses.size-1].name == "kotlin.coroutines.Continuation"
                }else{
                    false
                }
                val returnTypeClassName = if (suspendMethod){
                    returnTypeMap[oldMethodName+oldDescriptor] ?: (returnTypeMap[targetMethodName+oldDescriptor] ?: returnType.name)
                }else{
                    returnType.name
                }

                val returnStr = if (isSuspend){
                    String.format(
                        ClassNameToConversions.getReturnXObject(returnType.name), "pointCut.joinPointReturnExecute(${if (returnClassName == null) null else "$returnClassName.class"})"
                    )
                }else{
                    String.format(
                        ClassNameToConversions.getReturnXObject(returnType.name), "pointCut.joinPointExecute(${if (suspendMethod) "(kotlin.coroutines.Continuation)\$$len" else "null" })"
                    )
                }

                val returnStr2 = if (returnType.name == "void"){
                    "$returnStr;\nreturn;"
                }else{
                    "$returnStr;\n"
                }

                val invokeReturnStr:String? = ClassNameToConversions.getReturnInvokeXObject(returnType.name)
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


                ClassFileUtils.createInvokeClass(invokeStaticClass,invokeClassName,invokeBody, oldMethodName + oldDescriptor)
                ClassFileUtils.outputCacheDir?.let {
                    cp.appendClassPath(it.absolutePath)
                }
                cp.importPackage(realInvokeClassNameReal)
                val constructor = "\"${invokeClassName.replace(".", "_")}\",$targetClassName.class,${if(isStaticMethod)"null" else "\$0"},\"$oldMethodName\",\"$targetMethodName\",${value.lambda}"
                val setArgsStr =
                    "pointCut.setTarget(${if(isStaticMethod)"null" else "\$0"});"+
                    (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
                        (if (isHasArgs) "        pointCut.setArgs(args);\n" else "        pointCut.setArgs(null);\n")

                val invokeMethodStr = if (ClassFileUtils.reflectInvokeMethod){
                    if (ClassFileUtils.reflectInvokeMethodStatic){
                        "new $invokeStaticClass()"
                    }else{
                        "null"
                    }
                }else{
                    "new $invokeClassName()"
                }

                newMethodBody =
                    " {AndroidAopJoinPoint pointCut = ObjectGetUtils.INSTANCE.getAndroidAopJoinPoint($constructor);\n"+
                            "if(pointCut.isInit()){\n" +
                                setArgsStr+
                            "   $returnStr2\n" +
                            "}\n"+
                            "String[] cutClassNames = new String[]{$cutClassNameArray};\n"+
                            "pointCut.setCutMatchClassNames(cutClassNames);\n"+
                            "Class[] classes = new Class[]{$paramsClassesBuffer};\n"+
                            "pointCut.setArgClasses(classes);\n"+
                            "String[] paramNames = new String[]{$paramsNamesBuffer};\n"+
                            "pointCut.setParamNames(paramNames);\n"+
                            "pointCut.setReturnClass($returnTypeClassName.class);\n"+
                            "pointCut.setInvokeMethod($invokeMethodStr);\n"+
                            setArgsStr +
                            "        "+returnStr+";}"
                ctMethod.setBody(newMethodBody)
                InitConfig.putCutInfo(value)
            } catch (e: Exception) {
                printLog("newMethodBody=$newMethodBody")
                if (e is NotFoundException || e is CannotCompileException){
                    WovenInfoUtils.deleteAopMethodCutInnerClassInfoInvokeMethod(Utils.dotToSlash(targetClassName),targetMethodName,oldDescriptor)
                    ClassFileUtils.deleteInvokeClass(invokeClassName)
                    e.printStackTrace()
                }else{
                    throw e
                }
            }
        }
        val wovenBytes = ctClass.toBytecode()
//        ctClass.detach()
        return wovenBytes
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
        val className1 = "$thisClassName\$Inner${thisClassName.computeMD5()}"
        mv.visitTypeInsn(AdviceAdapter.NEW,Utils.dotToSlash(className1))
        mv.visitInsn(AdviceAdapter.DUP)
        mv.visitMethodInsn(AdviceAdapter.INVOKESPECIAL,Utils.dotToSlash(className1),"<init>","()V",false)
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
        val map: HashMap<String, String> = WovenInfoUtils.getAopInstances()
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

    fun createCollectClass(output:File) = runBlocking{
        val collectDirJobs = mutableListOf<Deferred<Unit>>()
        val classPool = ClassPoolUtils.getNewClassPool()
        WovenInfoUtils.getAopCollectClassMap().forEach {(key,value) ->
            if (value == null){
                return@forEach
            }
            val job = async(Dispatchers.IO) {
                val className = "$key\$Inner${key.computeMD5()}"
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
                outFile.checkExist(true)
                classByteData.saveFile(outFile)
            }
            collectDirJobs.add(job)
        }
        collectDirJobs.awaitAll()
    }

    fun deleteOtherCompileClass(project:Project, variantName:String){
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
