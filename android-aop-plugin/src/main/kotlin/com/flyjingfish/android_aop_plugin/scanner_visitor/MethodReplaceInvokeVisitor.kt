package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_ABSTRACT
import org.objectweb.asm.Opcodes.ACC_NATIVE

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
        if (mv != null && "<init>" != name && "<clinit>" != name && !WovenInfoUtils.containInvoke(className)) {
            val isAbstractMethod = access and ACC_ABSTRACT != 0
            val isNativeMethod = access and ACC_NATIVE != 0
            if (!isAbstractMethod && !isNativeMethod) {
                mv = MethodReplaceInvokeAdapter(api, mv)
            }
        }
        return mv
    }

    private inner class MethodReplaceInvokeAdapter(api: Int, methodVisitor: MethodVisitor?) :
        MethodVisitor(api, methodVisitor) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            val key = owner + name + descriptor
            val replaceMethodInfo = WovenInfoUtils.getReplaceMethodInfoUse(key)
            if (replaceMethodInfo != null && replaceMethodInfo.oldOwner == owner && replaceMethodInfo.oldMethodName == name && replaceMethodInfo.oldMethodDesc == descriptor) {
                // 注意，最后一个参数是false，会不会太武断呢？
                super.visitMethodInsn(Opcodes.INVOKESTATIC, replaceMethodInfo.newOwner, replaceMethodInfo.newMethodName, replaceMethodInfo.newMethodDesc, false)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }
}