package com.flyjingfish.android_aop_plugin.scanner_visitor

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class MethodReplaceInvokeAdapter(private val className:String,private val superName: String,private val methodAccess:Int,
                                 private val methodName:String,private val methodDesc:String, methodVisitor: MethodVisitor?) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) ,MethodReplaceInvokeAdapterUtils.SuperCall {

    val utils = MethodReplaceInvokeAdapterUtils(className,superName,methodAccess,methodName,methodDesc,this,this)

    override fun visitTypeInsn(opcode: Int, type: String) {
        utils.visitTypeInsn(opcode, type)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        utils.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }


    override fun visitEnd() {
        super.visitEnd()
//        if (utils.deleteNews.isNotEmpty()){
//            utils.onResultListener?.onBack(utils.deleteNews)
//        }
    }

    override fun superVisitTypeInsn(opcode: Int, type: String) {
        super.visitTypeInsn(opcode, type)
    }

    override fun superVisitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun superVisitVarInsn(opcode: Int, varIndex: Int) {
        super.visitVarInsn(opcode, varIndex)
    }

    override fun superVisitInsn(opcode: Int) {
        super.visitInsn(opcode)
    }

    override fun superVisitLdcInsn(value: Any?) {
        super.visitLdcInsn(value)
    }

}