package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

open class SearchSuspendClass(
    methodVisitor: MethodVisitor?,
    private val className: String,
    private val methodDescriptor: String,
    private val onResultListener : OnResultListener?
) :
    MethodVisitor(Opcodes.ASM9, methodVisitor) {

    interface OnResultListener{
        fun onBack()
    }

    private val isSuspend: Boolean =
        methodDescriptor.endsWith("Lkotlin/coroutines/Continuation;)Ljava/lang/Object;")
    private var invokeClassName: String? = null
    private var invokeClassNameCount = 0


    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {

        if (isSuspend && name == "<init>" && owner.startsWith("$className$")) {
            invokeClassName = owner
            invokeClassNameCount++
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
//        if (isSuspend) {
//            printLog("RemoveAnnotation==visitMethodInsn==owner=$owner,name=$name,descriptor=$descriptor")
//            if (owner == "kotlinx/coroutines/BuildersKt"
//                && name == "withContext"
//                && descriptor == "(Lkotlin/coroutines/CoroutineContext;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"
//            ) {
//                super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/flyjingfish/android_aop_core/utils/BuildersKtWithContext", name, descriptor, isInterface)
//            }else {
//                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
//            }
//        } else {
//            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
//        }
    }

    override fun visitEnd() {
        super.visitEnd()
        val suspendClassName = invokeClassName
        if (isSuspend && suspendClassName != null) {
            printLog("RemoveAnnotation==visitEnd==suspendClassName=$suspendClassName")
            WovenInfoUtils.addAopMethodCutInnerClassInfoInvokeClassName(suspendClassName,invokeClassNameCount)
            if (invokeClassNameCount > 1){
                onResultListener?.onBack()
            }
        }
    }
}