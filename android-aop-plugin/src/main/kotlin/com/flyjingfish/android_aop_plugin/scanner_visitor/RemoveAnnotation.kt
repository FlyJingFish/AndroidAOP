package com.flyjingfish.android_aop_plugin.scanner_visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class RemoveAnnotation(
    methodVisitor: MethodVisitor?
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return null
    }
}