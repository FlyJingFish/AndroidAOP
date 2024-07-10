package com.flyjingfish.android_aop_plugin.scanner_visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class RemoveAnnotation(
    methodVisitor: MethodVisitor?,
    className: String,
    methodDescriptor: String,
    onResultListener : OnResultListener
) :
    SearchSuspendClass(methodVisitor,className,methodDescriptor,onResultListener) {


    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return null
    }

}