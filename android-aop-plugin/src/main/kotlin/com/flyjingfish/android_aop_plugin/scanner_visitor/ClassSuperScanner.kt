package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ClassSuperInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addClassSuper
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ClassSuperScanner : ClassVisitor(Opcodes.ASM9) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        addClassSuper(ClassSuperInfo(name, superName, interfaces))
    }
}