package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class MethodReplaceInvokeAdapter(methodVisitor: MethodVisitor?) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {
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
            val shouldReplaceDesc: String = if (opcode == Opcodes.INVOKESTATIC) {
                descriptor
            } else {
                descriptor.replace("(", "(L$owner;")
            }
            if (shouldReplaceDesc == replaceMethodInfo.newMethodDesc) {
                InitConfig.addReplaceMethodInfo(replaceMethodInfo)
                // 注意，最后一个参数是false，会不会太武断呢？
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    replaceMethodInfo.newOwner,
                    replaceMethodInfo.newMethodName,
                    replaceMethodInfo.newMethodDesc,
                    false
                )
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }
}