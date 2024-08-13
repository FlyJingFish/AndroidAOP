package com.flyjingfish.android_aop_plugin.scanner_visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class SuspendReturnScanner : ClassVisitor(Opcodes.ASM9) {
    companion object{
        var hasSuspendReturn = false
    }

    private lateinit var className :String
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor {
        return ReplaceInvokeMethodVisitor()
    }

    inner class ReplaceInvokeMethodVisitor :
        MethodVisitor(Opcodes.ASM9) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            if (owner == "com/flyjingfish/android_aop_annotation/ProceedJoinPointSuspend"
                && ((name == "proceed" && (descriptor == "(Lcom/flyjingfish/android_aop_annotation/base/OnSuspendReturnListener;)Ljava/lang/Object;"
                                        || descriptor == "(Lcom/flyjingfish/android_aop_annotation/base/OnSuspendReturnListener;[Ljava/lang/Object;)Ljava/lang/Object;"))
                        || (name == "proceedIgnoreOther" && (descriptor == "(Lcom/flyjingfish/android_aop_annotation/base/OnSuspendReturnListener2;)Ljava/lang/Object;"
                                                        || descriptor == "(Lcom/flyjingfish/android_aop_annotation/base/OnSuspendReturnListener2;[Ljava/lang/Object;)Ljava/lang/Object;")))){
                hasSuspendReturn = true
            }
        }
    }
}