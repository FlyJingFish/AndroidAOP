package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.AopCollectClass
import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.CutFileJson
import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.utils.ClassFileUtils
import com.flyjingfish.android_aop_plugin.utils.ClassNameToConversions
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.Utils.CONVERSIONS_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.JOIN_POINT_CLASS
import com.flyjingfish.android_aop_plugin.utils.Utils.KEEP_CLASS
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import javassist.CannotCompileException
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.AttributeInfo
import javassist.bytecode.annotation.Annotation
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.AdviceAdapter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.regex.Matcher
import java.util.regex.Pattern

object WovenIntoCode {
    private const val METHOD_SUFFIX = "\$\$AndroidAOP"
    @Throws(Exception::class)
    fun modifyClass(
        inputStreamBytes: ByteArray?,
        methodRecordHashMap: HashMap<String, MethodRecord>,
        hasReplace:Boolean
    ): ByteArray {
        val wovenRecord = mutableListOf<MethodRecord>()

        val cr = ClassReader(inputStreamBytes)
        val cw = ClassWriter(cr, 0)
        fun visitMethod4Record(access: Int,
                        name: String,
                        descriptor: String,
                        signature: String?,
                        exceptions: Array<String?>?,
                        classNameMd5:String ?){
            methodRecordHashMap.forEach { (_: String, value: MethodRecord) ->
                val oldMethodName = value.methodName
                val oldDescriptor = value.descriptor
                val newMethodName = "$oldMethodName$$$classNameMd5$METHOD_SUFFIX"
                if (newMethodName == name && oldDescriptor == descriptor){
                    wovenRecord.add(value)
                }
            }
        }
        var thisHasCollect = false
        var thisHasStaticClock = false
        var thisCollectClassName :String ?= null
        if (hasReplace){
            cr.accept(object :MethodReplaceInvokeVisitor(cw){
                var classNameMd5:String ?= null
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String?,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access, name, signature, superName, interfaces)
                    classNameMd5 = Utils.computeMD5(Utils.slashToDot(name))
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
                    visitMethod4Record(access, name, descriptor, signature, exceptions, classNameMd5)
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
                var classNameMd5:String ?= null
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String?,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access, name, signature, superName, interfaces)
                    classNameMd5 = Utils.computeMD5(Utils.slashToDot(name))
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
                    visitMethod4Record(access, name, descriptor, signature, exceptions, classNameMd5)
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
            val oldMethodName = value.methodName
//            val targetMethodName = oldMethodName + METHOD_SUFFIX
            val oldDescriptor = value.descriptor
            cr.accept(object : ReplaceBaseClassVisitor(cw) {
                var classNameMd5:String ?= null
                lateinit var className: String
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String,
                    signature: String?,
                    superName: String?,
                    interfaces: Array<out String>?
                ) {
                    super.visit(version, access, name, signature, superName, interfaces)
                    classNameMd5 = Utils.computeMD5(Utils.slashToDot(name))
                    className = name
                }
                override fun visitAnnotation(
                    descriptor: String,
                    visible: Boolean
                ): AnnotationVisitor {
                    return object :
                        AnnotationVisitor(Opcodes.ASM9) {
                        override fun visitAnnotation(
                            name: String,
                            descriptor: String
                        ): AnnotationVisitor {
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
                        val newAccess = if (Utils.isStaticMethod(access)){
                            ACC_PUBLIC + ACC_STATIC + ACC_FINAL
                        }else{
                            ACC_PUBLIC + ACC_FINAL
                        }
                        var mv: MethodVisitor? = super.visitMethod(
                            newAccess,
                            "$oldMethodName$$$classNameMd5$METHOD_SUFFIX",
                            descriptor,
                            signature,
                            exceptions
                        )

                        if (hasReplace && mv != null && Utils.isHasMethodBody(access)) {
                            mv = MethodReplaceInvokeAdapter(className,"$name$descriptor",mv)
                        }
                        mv
                    } else {
                        null
                    }
                }

                override fun visitEnd() {
                    super.visitEnd()
                }
            }, 0)
        }
        thisCollectClassName?.let {
            if (thisHasCollect && !thisHasStaticClock){
                wovenStaticCode(cw, it)
            }
        }

        val cp = ClassPoolUtils.getNewClassPool()
//        val cp = ClassPool.getDefault();
        val byteArrayInputStream: InputStream =
            ByteArrayInputStream(cw.toByteArray())
        val ctClass = cp.makeClass(byteArrayInputStream)
        cp.importPackage(JOIN_POINT_CLASS)
        cp.importPackage(CONVERSIONS_CLASS)
        cp.importPackage(KEEP_CLASS)
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            val targetClassName = ctClass.name
            val oldMethodName = value.methodName
            val targetMethodName = "$oldMethodName$$${Utils.computeMD5(targetClassName)}$METHOD_SUFFIX"
            val oldDescriptor = value.descriptor
            val cutClassName = value.cutClassName
            val invokeClassName = "${targetClassName}\$Invoke${Utils.computeMD5(targetMethodName+oldDescriptor)}"
//            if (value in wovenRecord){
////                WovenInfoUtils.checkNoneInvokeClass(invokeClassName)
//                return@forEach
//            }
            try {
                val ctMethod =
                    getCtMethod(ctClass, oldMethodName, oldDescriptor)
                val targetMethod =
                    getCtMethod(ctClass, targetMethodName, oldDescriptor)
                if (ctMethod == null){
                    printLog("------ctMethod ${oldMethodName}${oldDescriptor} 方法找不到了-----")
                    return@forEach
                }else if (targetMethod == null){
                    printLog("------targetMethod ${targetMethodName}${oldDescriptor} 方法找不到了-----")
                    return@forEach
                }

                val ccFile = ctClass.classFile
                val constpool = ccFile.constPool

                // create the annotation
                val annotAttr =
                    AnnotationsAttribute(
                        constpool,
                        AnnotationsAttribute.visibleTag
                    )
                val annot =
                    Annotation(KEEP_CLASS, constpool)
                annotAttr.addAnnotation(annot)
                targetMethod.methodInfo.addAttribute(annotAttr)

                //给原有方法增加 @Keep，防止被混淆
                val attributeInfos :List<AttributeInfo> = ctMethod.methodInfo.attributes
                var annotationsAttribute :AnnotationsAttribute ?= null
                for (attributeInfo in attributeInfos) {
                    if (attributeInfo is AnnotationsAttribute){
                        annotationsAttribute = attributeInfo
                        break
                    }
                }
                if (annotationsAttribute == null){
                    annotationsAttribute = AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag)
                }
                val annotation = Annotation(KEEP_CLASS, constpool);
                annotationsAttribute.addAnnotation(annotation);
                ctMethod.methodInfo.addAttribute(annotationsAttribute);

//                val paramNames: MutableList<String> =
//                    ArrayList()
                val isStaticMethod =
                    Modifier.isStatic(ctMethod.modifiers)
                val argsBuffer = StringBuffer()

                val paramsClassNamesBuffer = StringBuffer()
                val paramsClassesBuffer = StringBuffer()
                val ctClasses = ctMethod.parameterTypes
                val returnType = ctMethod.returnType
                val len = ctClasses.size
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

                val returnStr = String.format(
                    ClassNameToConversions.getReturnXObject(returnType.name), "pointCut.joinPointExecute()"
                )

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


                ClassFileUtils.createInvokeClass(invokeClassName,invokeBody, oldMethodName + oldDescriptor)
                cp.importPackage(invokeClassName)
                val argReflect = if (ClassFileUtils.reflectInvokeMethod) "" else ",new $invokeClassName()"
                val constructor = "$targetClassName.class,${if(isStaticMethod)"null" else "\$0"},\"$oldMethodName\",\"$targetMethodName\"";
                val body =
                    " {AndroidAopJoinPoint pointCut = new AndroidAopJoinPoint($constructor);\n"+
                            (if (cutClassName != null) "        pointCut.setCutMatchClassName(\"$cutClassName\");\n" else "") +
//                            (if (isHasArgs) "        String[] classNames = new String[]{$paramsClassNamesBuffer};\n" else "") +
//                            (if (isHasArgs) "        pointCut.setArgClassNames(classNames);\n" else "") +
                            "Class[] classes = new Class[]{$paramsClassesBuffer};\n"+
                            "pointCut.setArgClasses(classes);\n"+
                            (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgs(args$argReflect);\n" else "        pointCut.setArgs(null$argReflect);\n") +
                            "        "+returnStr+";}"
                ctMethod.setBody(body)
                InitConfig.putCutInfo(value)
            } catch (e: NotFoundException) {
                throw RuntimeException(e)
            } catch (e: CannotCompileException) {
                throw RuntimeException(e)
            }
        }
        return ctClass.toBytecode()
    }

    fun wovenStaticCode(cw:ClassWriter,thisClassName:String){
        val mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null);
        val className1 = "$thisClassName\$Inner${Utils.computeMD5(thisClassName)}"
        mv.visitTypeInsn(AdviceAdapter.NEW,Utils.dotToSlash(className1));
        mv.visitInsn(AdviceAdapter.DUP);//压入栈
        //弹出一个对象所在的地址，进行初始化操作，构造函数默认为空，此时栈大小为1（到目前只有一个局部变量）
        mv.visitMethodInsn(AdviceAdapter.INVOKESPECIAL,Utils.dotToSlash(className1),"<init>","()V",false);
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 0)
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


    fun createInitClass(output:File) {
        val className = "com.flyjingfish.android_aop_annotation.utils.DebugAndroidAopInit"
        //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
        val cw = ClassWriter(COMPUTE_FRAMES or COMPUTE_MAXS)
        //生成一个public的类，类路径是com.study.Human
        cw.visit(V1_8, ACC_PUBLIC, Utils.dotToSlash(className), null, "java/lang/Object", null)

        //生成默认的构造方法： public Human()
        var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0) //更新操作数栈
        mv.visitEnd() //一定要有visitEnd


        //生成静态方法
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "<clinit>", "()V", null, null)
        //生成静态方法中的字节码指令
        val map: HashMap<String, String> = WovenInfoUtils.aopInstances
        if (map.isNotEmpty()) {
            map.forEach { (key, value) ->
                RegisterMapWovenInfoCode.registerCreators(mv,key, value)
                RegisterMapWovenInfoCode.registerMatchCreators(mv,key, value)
            }
        }


        mv.visitInsn(RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        //设置必要的类路径
        val path = output.absolutePath + "/" +Utils.dotToSlash(className)+".class"
        //获取类的byte数组
        val classByteData = cw.toByteArray()
        //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
        val outFile = File(path)
        if (!outFile.parentFile.exists()){
            outFile.parentFile.mkdirs()
        }
        if (!outFile.exists()){
            outFile.createNewFile()
        }
        ByteArrayInputStream(classByteData).use {inputStream->
            outFile.outputStream().use {
                inputStream.copyTo(it)
            }

        }
    }

    fun createCollectClass(output:File) {
        val classPool = ClassPoolUtils.getNewClassPool()
        WovenInfoUtils.aopCollectClassMap.forEach {(key,value) ->
            if (value == null){
                return@forEach
            }
            val className = "$key\$Inner${Utils.computeMD5(key)}"
            //新建一个类生成器，COMPUTE_FRAMES，COMPUTE_MAXS这2个参数能够让asm自动更新操作数栈
            val cw = ClassWriter(COMPUTE_FRAMES or COMPUTE_MAXS)
            //生成一个public的类，类路径是com.study.Human
            cw.visit(V1_8, ACC_PUBLIC+ACC_STATIC, Utils.dotToSlash(className), null, "java/lang/Object", null)

            //生成默认的构造方法： public Human()
            var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            mv.visitInsn(RETURN)
            mv.visitMaxs(0, 0) //更新操作数栈
            mv.visitEnd() //一定要有visitEnd


            //生成静态方法
            mv = cw.visitMethod(ACC_PUBLIC+ACC_STATIC, "<clinit>", "()V", null, null);
            //生成静态方法中的字节码指令
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
                            val isObject = Utils.slashToDotClassName(aopCollectCut.collectClassName) == "java.lang.Object"
                            var isMatchExtends = false
                            val collectExtendsClassName = aopCollectCut.collectExtendsClassName
                            if (!isObject){
                                var isDirectExtends = false
                                var isImplementsInterface = false
                                val interfaces = ctClass.interfaces
                                if (interfaces != null) {
                                    for (subCtClass in ctClass.interfaces) {
                                        if (Utils.slashToDotClassName(subCtClass.name) == aopCollectCut.collectClassName){
                                            isImplementsInterface = true
                                            break
                                        }
                                    }
                                }

                                if (isImplementsInterface || Utils.slashToDotClassName(aopCollectCut.collectClassName) == Utils.slashToDotClassName(
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
                                            isExtends = Utils.isInstanceof(
                                                clsName,
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
                                            val isInstanceof = Utils.isInstanceof(
                                                clsName,
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
                        } catch (_: Exception) {
                        }
                    }
                }
            }


            mv.visitInsn(RETURN)
            mv.visitMaxs(0, 0)
            mv.visitEnd()
            //设置必要的类路径
            val path = output.absolutePath + "/" +Utils.dotToSlash(className)+".class"
            //获取类的byte数组
            val classByteData = cw.toByteArray()
            //把类数据写入到class文件,这样你就可以把这个类文件打包供其他的人使用
            val outFile = File(path)
            if (!outFile.parentFile.exists()){
                outFile.parentFile.mkdirs()
            }
            if (!outFile.exists()){
                outFile.createNewFile()
            }else{
                outFile.delete()
                outFile.createNewFile()
            }
            ByteArrayInputStream(classByteData).use {inputStream->
                outFile.outputStream().use {
                    inputStream.copyTo(it)
                }

            }
        }

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
