package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.utils.ClassNameToConversions
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.ByteArrayInputStream
import java.io.InputStream


object WovenIntoCode {
    private const val METHOD_SUFFIX = "\$\$AndroidAOP"
    @Throws(Exception::class)
    fun modifyClass(
        inputStreamBytes: ByteArray?,
        methodRecordHashMap: HashMap<String, MethodRecord>
    ): ByteArray {
        val cr = ClassReader(inputStreamBytes)
        val cw = ClassWriter(cr, 0)
        cr.accept(object : ClassVisitor(Opcodes.ASM4, cw) {}, 0)
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            val oldMethodName = value.methodName
            val targetMethodName = oldMethodName + METHOD_SUFFIX
            val oldDescriptor = value.descriptor
            cr.accept(object : ClassVisitor(Opcodes.ASM4, cw) {
                override fun visitAnnotation(
                    descriptor: String,
                    visible: Boolean
                ): AnnotationVisitor {
                    return object :
                        AnnotationVisitor(Opcodes.ASM8) {
                        override fun visitAnnotation(
                            name: String,
                            descriptor: String
                        ): AnnotationVisitor {
                            return super.visitAnnotation(name, descriptor)
                        }
                    }
                }

                override fun visitMethod(
                    access: Int,
                    name: String,
                    descriptor: String,
                    signature: String?,
                    exceptions: Array<String>?
                ): MethodVisitor? {
                    return if (oldMethodName == name && oldDescriptor == descriptor) {
                        if (oldMethodName == "testTopFun"){
                            printLog("visitMethod,access=$access")
                        }
                        val newAccess = when(access){
                            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                            Opcodes.ACC_STATIC,
                            Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                            Opcodes.ACC_PROTECTED + Opcodes.ACC_STATIC,
                            Opcodes.ACC_PROTECTED + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC ,
                            Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
                            -> Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC
                            else -> Opcodes.ACC_PUBLIC
                        }
                        super.visitMethod(
                            newAccess,
                            targetMethodName,
                            descriptor,
                            signature,
                            exceptions
                        )
                    } else {
                        null
                    }
                }

                override fun visitEnd() {
                    super.visitEnd() //注意原本的visiEnd不能少
                }
            }, 0)
        }
//        val cp = ClassPoolUtils.classPool
        val cp = ClassPool(null)
        cp.appendSystemPath()
//        System.out.println(WovenInfoUtils.INSTANCE.getClassPaths());
        for (classPath in WovenInfoUtils.classPaths){
            try {
                cp.appendClassPath(classPath)
            } catch (e: NotFoundException) {
                throw RuntimeException(e)
            }
        }
//        ClassPool cp = ClassPool.getDefault();
        val byteArrayInputStream: InputStream =
            ByteArrayInputStream(cw.toByteArray())
        val ctClass = cp.makeClass(byteArrayInputStream)
        cp.importPackage("com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint")
        cp.importPackage("com.flyjingfish.android_aop_annotation.Conversions")
        cp.importPackage("androidx.annotation.Keep")
        methodRecordHashMap.forEach { (key: String, value: MethodRecord) ->
            val oldMethodName = value.methodName
            val targetMethodName = oldMethodName + METHOD_SUFFIX
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
                targetMethod!!.methodInfo.addAttribute(annotAttr)
//                val paramNames: MutableList<String> =
//                    ArrayList()
                val isStaticMethod =
                    Modifier.isStatic(ctMethod!!.modifiers)
                val argsBuffer = StringBuffer()

                val paramsClassNamesBuffer = StringBuffer()
                val ctClasses = ctMethod.parameterTypes
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
                val allSignature = ctMethod.signature
                val returnStr = String.format(
                    ClassNameToConversions.getReturnXObject(
                        allSignature.substring(allSignature.indexOf(")") + 1)
                    ), "pointCut.joinPointExecute()"
                )
                val targetClassName = ctClass.name
                val body =
                    """ {AndroidAopJoinPoint pointCut = new AndroidAopJoinPoint(${if (isStaticMethod) "\"$targetClassName\",\"$oldMethodName\",\"$targetMethodName\"" else "$0,\"$oldMethodName\",\"$targetMethodName\""});"""+
                            (if (cutClassName != null) "        pointCut.setCutMatchClassName(\"$cutClassName\");\n" else "") +
                            (if (isHasArgs) "        String[] classNames = new String[]{$paramsClassNamesBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgClassNames(classNames);\n" else "") +
                            (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgs(args);\n" else "        pointCut.setArgs(null);\n") +
                            "        "+returnStr+";}"
                printLog(body)
                ctMethod.setBody(body)
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
}
