package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.InitConfig
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class MethodReplaceInvokeAdapter(private val className:String,private val superName: String,
                                 private val methodName:String,private val methodDesc:String, methodVisitor: MethodVisitor?) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {
    private val methodNameDesc = Utils.getRealMethodName(methodName)+methodDesc
    private val isConstructorMethod = methodNameDesc.startsWith("<init>(")
    private val canReplaceMethod = !(superName == "kotlin/coroutines/jvm/internal/ContinuationImpl" && methodNameDesc == "invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;" )

    interface OnResultListener{
        fun onBack()
    }
    var onResultListener : OnResultListener ?= null


    override fun visitTypeInsn(opcode: Int, type: String) {
        if (opcode == Opcodes.NEW) {
            val replaceMethodInfo = getReplaceInfo(type, "<init>", "")
            if (replaceMethodInfo != null && replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.newClassName.isNotEmpty()){
                super.visitTypeInsn(opcode, replaceMethodInfo.newClassName)
                onResultListener?.onBack()
                return
            }
        }
        super.visitTypeInsn(opcode, type)
    }

    private fun getReplaceInfo(owner: String,
                        name: String,
                        descriptor: String):ReplaceMethodInfo?{
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
        return if (isReplaceClass){
            replaceMethodInfo
        }else {
            null
        }
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val isInMethodInner = opcode == Opcodes.INVOKESPECIAL && owner == superName  && name == methodName && descriptor == methodDesc

        var replaceMethodInfo = getReplaceInfo(owner, name, "")
        var isReplaceClass = replaceMethodInfo != null && replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.newClassName.isNotEmpty()
        if (!isReplaceClass){
            val methodReplaceMethodInfo = getReplaceInfo(owner, name, descriptor)
            if (methodReplaceMethodInfo != null){
                replaceMethodInfo = methodReplaceMethodInfo
            }
            isReplaceClass = replaceMethodInfo != null
        }

        if (isReplaceClass && replaceMethodInfo != null
            && (!className.contains(replaceMethodInfo.newOwner) || methodNameDesc != "${replaceMethodInfo.newMethodName}${replaceMethodInfo.newMethodDesc}")) {
            val shouldReplace: Boolean = if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW){
                if (isConstructorMethod){
                    !(owner == superName && name == "<init>")
                }else{
                    "" == replaceMethodInfo.oldMethodDesc
                }
            }else if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT) {
                if (isConstructorMethod){
                    !(owner == superName && name == "<init>")
                }else{
                    descriptor == replaceMethodInfo.oldMethodDesc
                }
            } else if (opcode == Opcodes.INVOKESTATIC) {
                descriptor == replaceMethodInfo.newMethodDesc
            } else {
                descriptor.replace("(", "(L${replaceMethodInfo.oldOwner};") == replaceMethodInfo.newMethodDesc || descriptor.replace("(", "(Ljava/lang/Object;") == replaceMethodInfo.newMethodDesc
            }
            if (shouldReplace && !isInMethodInner) {
                val isThisInit = owner == className && methodName == "<init>" && methodName == name
                val isInitAop = replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.INIT
                if (isInitAop && isThisInit){
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    return
                }
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && replaceMethodInfo.isCallNew()) {
                    super.visitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                }else if (isInitAop) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                InitConfig.addReplaceMethodInfo(replaceMethodInfo)
                if (replaceMethodInfo.replaceType == ReplaceMethodInfo.ReplaceType.NEW && !replaceMethodInfo.isCallNew()){
                    super.visitMethodInsn(opcode, replaceMethodInfo.newClassName, name, descriptor, isInterface)
                    onResultListener?.onBack()
                }else{
                    if (canReplaceMethod){
                        // 注意，最后一个参数是false，会不会太武断呢？
                        super.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            replaceMethodInfo.newOwner,
                            replaceMethodInfo.newMethodName,
                            replaceMethodInfo.newMethodDesc,
                            false
                        )
                        onResultListener?.onBack()
                    }else {
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    }
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }
}