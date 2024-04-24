package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.utils.ClassNameToConversions
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
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
import org.objectweb.asm.*
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
                }
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String?>?
                ): MethodVisitor? {
                    methodRecordHashMap.forEach { (_: String, value: MethodRecord) ->
                        val oldMethodName = value.methodName
                        val oldDescriptor = value.descriptor
                        val newMethodName = "$oldMethodName$$$classNameMd5$METHOD_SUFFIX"
                        if (newMethodName == name && oldDescriptor == descriptor){
                            wovenRecord.add(value)
                            InitConfig.putCutInfo(value)
                        }
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions)
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
                }
                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String?>?
                ): MethodVisitor? {
                    methodRecordHashMap.forEach { (_: String, value: MethodRecord) ->
                        val oldMethodName = value.methodName
                        val oldDescriptor = value.descriptor
                        val newMethodName = "$oldMethodName$$$classNameMd5$METHOD_SUFFIX"
                        if (newMethodName == name && oldDescriptor == descriptor){
                            wovenRecord.add(value)
                        }
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions)
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
                    exceptions: Array<String>?
                ): MethodVisitor? {
                    return if (oldMethodName == name && oldDescriptor == descriptor) {
                        val newAccess = if (Utils.isStaticMethod(access)){
                            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL
                        }else{
                            Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL
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
        val cp = ClassPoolUtils.getNewClassPool()
//        val cp = ClassPool.getDefault();
        val byteArrayInputStream: InputStream =
            ByteArrayInputStream(cw.toByteArray())
        val ctClass = cp.makeClass(byteArrayInputStream)
        cp.importPackage("com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint")
        cp.importPackage("com.flyjingfish.android_aop_annotation.Conversions")
        cp.importPackage("androidx.annotation.Keep")
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            if (value in wovenRecord){
                return@forEach
            }
            val targetClassName = ctClass.name
            val oldMethodName = value.methodName
            val targetMethodName = "$oldMethodName$$${Utils.computeMD5(targetClassName)}$METHOD_SUFFIX"
            val oldDescriptor = value.descriptor
            val cutClassName = value.cutClassName
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
                    Annotation("androidx.annotation.Keep", constpool)
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
                val annotation = Annotation("androidx.annotation.Keep", constpool);
                annotationsAttribute.addAnnotation(annotation);
                ctMethod.methodInfo.addAttribute(annotationsAttribute);

//                val paramNames: MutableList<String> =
//                    ArrayList()
                val isStaticMethod =
                    Modifier.isStatic(ctMethod.modifiers)
                val argsBuffer = StringBuffer()

                val paramsClassNamesBuffer = StringBuffer()
                val ctClasses = ctMethod.parameterTypes
                val returnType = ctMethod.returnType
                val len = ctClasses.size
                // 非静态的成员函数的第一个参数是this
                val pos =  1
                val isHasArgs = len > 0
                for (i in ctClasses.indices) {
                    val aClass = ctClasses[i]
                    val name = aClass.name

                    paramsClassNamesBuffer.append("\"").append(name).append("\"")

                    val index = i + pos

                    argsBuffer.append(String.format(ClassNameToConversions.getArgsXObject(name), "\$"+index))

                    if (i != len - 1) {
                        paramsClassNamesBuffer.append(",")
                        argsBuffer.append(",")
                    }
                }

                val returnStr = String.format(
                    ClassNameToConversions.getReturnXObject(returnType.name), "pointCut.joinPointExecute()"
                )

                val constructor = "$targetClassName.class,${if(isStaticMethod)"null" else "\$0"},\"$oldMethodName\",\"$targetMethodName\"";
                val body =
                    """ {AndroidAopJoinPoint pointCut = new AndroidAopJoinPoint($constructor);"""+
                            (if (cutClassName != null) "        pointCut.setCutMatchClassName(\"$cutClassName\");\n" else "") +
                            (if (isHasArgs) "        String[] classNames = new String[]{$paramsClassNamesBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgClassNames(classNames);\n" else "") +
                            (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgs(args);\n" else "        pointCut.setArgs(null);\n") +
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
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "init", "()V", null, null)
        //生成静态方法中的字节码指令
        val map: HashMap<String, String> = WovenInfoUtils.aopInstances
        if (map.isNotEmpty()) {
            map.forEach { (key, value) ->
                val methodVisitor = mv
                if (key.startsWith("@")){

                    methodVisitor.visitLdcInsn(key)

                    methodVisitor.visitTypeInsn(NEW, value)
                    methodVisitor.visitInsn(DUP)
                    methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        value,
                        "<init>",
                        "()V",
                        false
                    )

                    methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        Utils.dotToSlash(Utils.JoinAnnoCutUtils),
                        "registerCreator",
                        "(Ljava/lang/String;Lcom/flyjingfish/android_aop_annotation/base/BasePointCutCreator;)V",
                        false
                    )
                }else{
                    methodVisitor.visitLdcInsn(key)

                    methodVisitor.visitTypeInsn(NEW, value)
                    methodVisitor.visitInsn(DUP)
                    methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        value,
                        "<init>",
                        "()V",
                        false
                    )

                    methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        Utils.dotToSlash(Utils.JoinAnnoCutUtils),
                        "registerMatchCreator",
                        "(Ljava/lang/String;Lcom/flyjingfish/android_aop_annotation/base/MatchClassMethodCreator;)V",
                        false
                    )
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
        }
        ByteArrayInputStream(classByteData).use {
            it.copyTo(FileOutputStream(outFile))
        }
    }
}
