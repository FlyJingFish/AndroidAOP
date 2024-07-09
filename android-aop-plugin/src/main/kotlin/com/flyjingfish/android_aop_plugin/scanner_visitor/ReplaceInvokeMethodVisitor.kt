package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ReplaceInvokeMethodVisitor(methodVisitor: MethodVisitor?,private val clazzName:String,
                                 private val superClazzName:String) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {
    interface OnResultListener{
        fun onBack()
    }
    var onResultListener : OnResultListener?= null
    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val info = WovenInfoUtils.getAopMethodCutInnerClassInfo(owner,name,descriptor,clazzName,superClazzName)
        val invokeName = if (info == null){
            name
        }else{
            if (owner == info.className
                && info.methodName == name
                && descriptor == info.methodDescriptor
                && WovenInfoUtils.isHasAopMethodCutInnerClassInfoInvokeMethod(owner,info.targetMethodName,descriptor)){
                onResultListener?.onBack()
                info.targetMethodName
            }else{
                name
            }
        }
        super.visitMethodInsn(opcode, owner, invokeName, descriptor, isInterface)
    }
}