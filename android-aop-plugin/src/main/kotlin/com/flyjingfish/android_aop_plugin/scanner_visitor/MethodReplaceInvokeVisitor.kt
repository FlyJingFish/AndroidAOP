package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.isHasMethodBody
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

open class MethodReplaceInvokeVisitor(
    classVisitor: ClassVisitor
) : ReplaceBaseClassVisitor(classVisitor) {
    lateinit var className: String
    lateinit var superName: String
    var replaced = false
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        this.superName = superName
    }
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        var mv: MethodVisitor? = super.visitMethod(access, name, descriptor, signature, exceptions)

        if (mv != null && access.isHasMethodBody()) {
            mv = MethodReplaceInvokeAdapter(className,superName,name,descriptor,mv)
            mv.onResultListener = object : MethodReplaceInvokeAdapter.OnResultListener{
                override fun onBack() {
                    replaced = true
                }
            }
        }
        return mv
    }

}