package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodReplaceInvokeVisitor(
    classVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, classVisitor) {
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
        className = Utils.slashToDotClassName(name)
    }
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        var mv: MethodVisitor? = super.visitMethod(access, name, descriptor, signature, exceptions)

        if (mv != null && "<init>" != name && "<clinit>" != name && WovenInfoUtils.isReplaceMethod(className) && Utils.isHasMethodBody(access)) {
            mv = MethodReplaceInvokeAdapter(mv)
        }
        return mv
    }

}