package com.flyjingfish.android_aop_plugin.scanner_visitor


import com.flyjingfish.android_aop_plugin.beans.MethodRecord
import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.Utils.slashToDotClassName
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode


class ReplaceMethodScanner :
    ClassNode(Opcodes.ASM9) {
    lateinit var className: String
    private var replaceInvokeClassName: String? = null
    private var replaceTargetClassName: String? = null


    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        className = name
        val seeClsName = slashToDotClassName(className)
        val isReplaceClass = WovenInfoUtils.containReplace(seeClsName)
        if (isReplaceClass) {
            replaceInvokeClassName = WovenInfoUtils.getReplaceClassName(seeClsName)
            replaceTargetClassName = seeClsName
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return super.visitAnnotation(descriptor, visible)
    }

    companion object {
        const val REPLACE_POINT =
            "Lcom/flyjingfish/android_aop_annotation/anno/AndroidAopReplaceMethod"
    }

    open inner class MyMethodVisitor
        (
        val access: Int,
        val methodname: String,
        val methoddescriptor: String,
        val signature: String?,
        val exceptions: Array<String?>?, var methodName: MethodRecord
    ) : MethodNode(
        Opcodes.ASM9, access,
        methodname,
        methoddescriptor,
        signature,
        exceptions
    ) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (replaceTargetClassName != null) {
                val replaceMethodInfo = ReplaceMethodInfo(
                    className, methodname, methoddescriptor,
                    "", "", ""
                )

            }
            return super.visitAnnotation(descriptor, visible)
        }

    }

    override fun visitMethod(
        access: Int, name: String, descriptor: String,
        signature: String?, exceptions: Array<String?>?
    ): MethodVisitor {
        val myMethodVisitor = MyMethodVisitor(
            access, name, descriptor, signature, exceptions, MethodRecord(name, descriptor, null)
        )
        methods.add(myMethodVisitor)
        return myMethodVisitor
    }


}