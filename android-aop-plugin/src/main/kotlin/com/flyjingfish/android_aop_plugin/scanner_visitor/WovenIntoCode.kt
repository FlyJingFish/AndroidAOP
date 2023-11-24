package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.utils.ClassPoolUtils
import com.flyjingfish.android_aop_plugin.utils.Conversions
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.LocalVariableAttribute
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
            val targetMethodName = oldMethodName + "AndroidAopAutoMethod"
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
                        super.visitMethod(
                            Opcodes.ACC_PUBLIC,
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
        var cp = ClassPool(null)
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
            val targetMethodName = oldMethodName + "AndroidAopAutoMethod"
            val oldDescriptor = value.descriptor
            val cutClassName = value.cutClassName
            try {
                val ctMethod =
                    getCtMethod(ctClass, oldMethodName, oldDescriptor)
                val targetMethod =
                    getCtMethod(ctClass, targetMethodName, oldDescriptor)
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
                val paramNames: MutableList<String> =
                    ArrayList()
                val methodInfo = ctMethod!!.methodInfo
                val codeAttribute = methodInfo.codeAttribute
                val attr =
                    codeAttribute.getAttribute(LocalVariableAttribute.tag) as LocalVariableAttribute
                val argsBuffer = StringBuffer()
                val isStaticMethod =
                    Modifier.isStatic(ctMethod.modifiers)
                var isHasArgs = false
                val paramsClassNames: List<String> =
                    ArrayList()
                val paramsClassNamesBuffer = StringBuffer()
                if (attr != null) {
                    val ctClasses = ctMethod.parameterTypes
                    for (i in ctClasses.indices) {
                        val aClass = ctClasses[i]
                        val name = aClass.name
                        paramsClassNamesBuffer.append("\"").append(name).append("\"")
                        if (i != ctClasses.size - 1) {
                            argsBuffer.append(",")
                        }
                    }
                    val len = ctClasses.size
                    // 非静态的成员函数的第一个参数是this
                    val pos = if (isStaticMethod) 0 else 1
                    isHasArgs = len > 0
                    for (i in 0 until len) {
                        val index = i + pos
                        val signature = attr.signature(i + pos)
                        argsBuffer.append(
                                Conversions.getArgsXObject(
                                    signature
                                ), "$$index"
                        )
                        if (i != len - 1) {
                            argsBuffer.append(",")
                        }
                        paramNames.add(attr.variableName(i + pos))
                    }
                }
                val allSignature = ctMethod.signature
                val returnStr = String.format(
                    Conversions.getReturnXObject(
                        allSignature.substring(allSignature.indexOf(")") + 1)
                    ), "pointCut.joinPointExecute()"
                )
                val body =
                    """ {AndroidAopJoinPoint pointCut = new AndroidAopJoinPoint(${if (isStaticMethod) "$0" else "$0,\"$oldMethodName\",\"$targetMethodName\""});"""+
                            (if (cutClassName != null) "        pointCut.setCutMatchClassName(\"$cutClassName\");\n" else "") +
                            (if (isHasArgs) "        String[] classNames = new String[]{$paramsClassNamesBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgClassNames(classNames);\n" else "") +
                            (if (isHasArgs) "        Object[] args = new Object[]{$argsBuffer};\n" else "") +
                            (if (isHasArgs) "        pointCut.setArgs(args);\n" else "        pointCut.setArgs(null);\n") +
                            "        "+returnStr+";}"
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
        if (ctMethods != null && ctMethods.size > 0) {
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
