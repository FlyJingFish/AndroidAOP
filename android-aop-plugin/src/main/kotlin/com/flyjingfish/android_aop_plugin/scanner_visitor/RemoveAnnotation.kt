package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor

class RemoveAnnotation(
    methodVisitor: MethodVisitor?,
    val className: String,
    val methodName: String,
    val methodDescriptor: String,
    onResultListener : OnResultListener,
    val onParamsCallback: OnParamsCallback
) :
    SearchSuspendClass(methodVisitor,className,methodDescriptor,onResultListener) {
    private val paramNames = mutableListOf<String>()

    interface OnParamsCallback{
        fun onBack(paramNames:List<String>)
    }


    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return null
    }

    override fun visitLocalVariable(
        name: String,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
        if (name != "this") {
            printLog("className=$className,methodName=$methodName,methodDescriptor=$methodDescriptor,Parameter name= $name");
            paramNames.add(name)
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        onParamsCallback.onBack(paramNames)
    }
}