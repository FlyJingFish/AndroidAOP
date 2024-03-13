package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addAnnoInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addMatchInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addModifyExtendsClassInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addReplaceInfo
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.Logger

class SearchAOPConfigVisitor(val logger: Logger) : ClassVisitor(Opcodes.ASM9) {
    var isAndroidAopClass = false
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (descriptor.contains(CLASS_POINT)) {
            isAndroidAopClass = true
        }
        return super.visitAnnotation(descriptor, visible)
    }

    internal inner class MethodAnnoVisitor : AnnotationVisitor(Opcodes.ASM9) {
        private var anno: String? = null
        private var cutClassName: String? = null
        private var baseClassName: String? = null
        private var methodNames: String? = null
        private var pointCutClassName: String? = null
        private var matchType = "EXTENDS"
        private var excludeClasses: String? = null
        override fun visit(name: String, value: Any) {
            if (isAndroidAopClass) {
                if (name == "value") {
                    anno = value.toString()
                }
                if (name == "pointCutClassName") {
                    cutClassName = value.toString()
                }
                if (name == "baseClassName") {
                    baseClassName = value.toString()
                }
                if (name == "methodNames") {
                    methodNames = value.toString()
                }
                if (name == "pointCutClassName") {
                    pointCutClassName = value.toString()
                }
                if (name == "matchType") {
                    matchType = value.toString()
                }
                if (name == "excludeClasses") {
                    excludeClasses = value.toString()
                }
                //                WovenInfoUtils.INSTANCE.addAnnoInfo(value.toString());
            }
            super.visit(name, value)
        }

        override fun visitEnd() {
            super.visitEnd()
            if (anno != null && cutClassName != null) {
                val cut = AopMethodCut(anno!!, cutClassName!!)
                addAnnoInfo(cut)
            }
            if (baseClassName != null && methodNames != null) {
                var strings: Array<String>? = null
                if (excludeClasses != null) {
                    strings = excludeClasses!!.split("-").toTypedArray()
                }
                val cut = AopMatchCut(
                    baseClassName!!,
                    methodNames!!.split("-").toTypedArray(),
                    pointCutClassName!!,
                    matchType,
                    strings
                )
                addMatchInfo(cut)
            }
        }
    }

    internal inner class ReplaceMethodVisitor : AnnotationVisitor(Opcodes.ASM9) {
        private var targetClassName: String? = null
        private var invokeClassName: String? = null
        override fun visit(name: String, value: Any) {
            if (isAndroidAopClass) {
                if (name == "targetClassName") {
                    targetClassName = value.toString()
                }
                if (name == "invokeClassName") {
                    invokeClassName = value.toString()
                }
            }
            super.visit(name, value)
        }

        override fun visitEnd() {
            super.visitEnd()
            if (targetClassName != null && invokeClassName != null) {
                addReplaceInfo(targetClassName!!, invokeClassName!!)
            }
        }
    }

    internal inner class ReplaceExtendsClassVisitor : AnnotationVisitor(Opcodes.ASM9) {
        private var targetClassName: String? = null
        private var extendsClassName: String? = null
        override fun visit(name: String, value: Any) {
            if (isAndroidAopClass) {
                if (name == "targetClassName") {
                    targetClassName = value.toString()
                }
                if (name == "extendsClassName") {
                    extendsClassName = value.toString()
                }
            }
            super.visit(name, value)
        }

        override fun visitEnd() {
            super.visitEnd()
            if (targetClassName != null && extendsClassName != null) {
                addModifyExtendsClassInfo(targetClassName!!, extendsClassName!!)
            }
        }
    }

    internal inner class MyMethodVisitor : MethodVisitor(Opcodes.ASM9) {
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            return if (isAndroidAopClass) {
                if (descriptor.contains(METHOD_POINT) || descriptor.contains(MATCH_POINT)) {
                    MethodAnnoVisitor()
                } else if (descriptor.contains(REPLACE_POINT)) {
                    ReplaceMethodVisitor()
                } else if (descriptor.contains(EXTENDS_POINT)) {
                    ReplaceExtendsClassVisitor()
                } else {
                    super.visitAnnotation(descriptor, visible)
                }
            } else {
                super.visitAnnotation(descriptor, visible)
            }
        }
    }

    override fun visitMethod(
        access: Int, name: String, descriptor: String?,
        signature: String?, exceptions: Array<String>?
    ): MethodVisitor? {
        return if (isAndroidAopClass) {
            MyMethodVisitor()
        } else {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }
    }

    companion object {
        const val CLASS_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopClass"
        const val METHOD_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopPointCut"
        const val MATCH_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopMatchClassMethod"
        const val REPLACE_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopReplaceMethod"
        const val EXTENDS_POINT =
            "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopModifyExtendsClass"
    }
}