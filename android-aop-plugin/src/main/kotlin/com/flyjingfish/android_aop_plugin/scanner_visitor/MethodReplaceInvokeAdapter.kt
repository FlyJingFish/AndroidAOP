package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class MethodReplaceInvokeAdapter(private val className:String,private val superName: String,
                                 private val methodNameDesc:String, methodVisitor: MethodVisitor?) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {
    val isConstructorMethod = methodNameDesc.startsWith("<init>(")

    interface OnResultListener{
        fun onBack()
    }
    var onResultListener : OnResultListener ?= null
    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val key = owner + name + descriptor
        var replaceMethodInfo = WovenInfoUtils.getReplaceMethodInfoUse(key)
        var isReplaceClass = false
        if (replaceMethodInfo == null){
            val oldOwner = WovenInfoUtils.getRealReplaceInfo(owner)
            if (oldOwner != null){
                val oldKey = oldOwner + name + descriptor
                replaceMethodInfo = WovenInfoUtils.getReplaceMethodInfoUse(oldKey)
                isReplaceClass = replaceMethodInfo != null && replaceMethodInfo.oldOwner == oldOwner && replaceMethodInfo.oldMethodName == name && replaceMethodInfo.oldMethodDesc == descriptor
            }
        }else{
            isReplaceClass = replaceMethodInfo.oldOwner == owner && replaceMethodInfo.oldMethodName == name && replaceMethodInfo.oldMethodDesc == descriptor
        }
        if (isReplaceClass && replaceMethodInfo != null
            && (!className.contains(replaceMethodInfo.newOwner) || methodNameDesc != "${replaceMethodInfo.newMethodName}${replaceMethodInfo.newMethodDesc}")) {
            val shouldReplace: Boolean = if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT||replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW) {
                if (isConstructorMethod){
                    !(owner == superName && name == "<init>")
                }else{
                    descriptor == replaceMethodInfo.oldMethodDesc
                }
            } else if (opcode == Opcodes.INVOKESTATIC) {
                descriptor == replaceMethodInfo.newMethodDesc
            } else {
                descriptor.replace("(", "(L${replaceMethodInfo.oldOwner};") == replaceMethodInfo.newMethodDesc
            }
            if (shouldReplace) {
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }else if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.isCallNew()) {
                    super.visitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                }
                InitConfig.addReplaceMethodInfo(replaceMethodInfo)
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && !replaceMethodInfo.isCallNew()){
                    super.visitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                }else{
                    // 注意，最后一个参数是false，会不会太武断呢？
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        replaceMethodInfo.newOwner,
                        replaceMethodInfo.newMethodName,
                        replaceMethodInfo.newMethodDesc,
                        false
                    )
                }
                onResultListener?.onBack()
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }
}