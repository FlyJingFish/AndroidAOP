package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.AopCollectCut
import com.flyjingfish.android_aop_plugin.beans.AopMatchCut
import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.beans.AopReplaceCut
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addAnnoInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addAopInstance
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addCollectConfig
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addMatchInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addModifyExtendsClassInfo
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addReplaceCut
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils.addReplaceInfo
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.Logger

class SearchAOPConfigVisitor() : ClassVisitor(Opcodes.ASM9) {
    var isAndroidAopClass = false
    lateinit var className: String
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
//                printLog("addAopInstance==$anno className=$className")
                addAopInstance(anno!!, className)
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
                addAopInstance(pointCutClassName!!, className)
            }
        }
    }

    internal inner class ReplaceMethodVisitor : AnnotationVisitor(Opcodes.ASM9) {
        private var targetClassName: String? = null
        private var invokeClassName: String? = null
        private var matchType = "EXTENDS"
        private var excludeClasses: String? = null
        override fun visit(name: String, value: Any) {
            if (isAndroidAopClass) {
                if (name == "targetClassName") {
                    targetClassName = value.toString()
                }
                if (name == "invokeClassName") {
                    invokeClassName = value.toString()
                }
                if (name == "matchType") {
                    matchType = value.toString()
                }
                if (name == "excludeClasses") {
                    excludeClasses = value.toString()
                }
            }
            super.visit(name, value)
        }

        override fun visitEnd() {
            super.visitEnd()
            if (targetClassName != null && invokeClassName != null) {
                addReplaceInfo(targetClassName!!, invokeClassName!!)
                var strings: Array<String>? = null
                if (excludeClasses != null) {
                    strings = excludeClasses!!.split("-").toTypedArray()
                }

                addReplaceCut(AopReplaceCut(targetClassName!!,invokeClassName!!,matchType,strings))
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


    internal inner class CollectClassVisitor : AnnotationVisitor(Opcodes.ASM9) {
        private var collectClassName: String? = null
        private var invokeClassName: String? = null
        private var invokeMethod: String? = null
        private var isClazz: String = "false"
        override fun visit(name: String, value: Any) {
            if (isAndroidAopClass) {
                if (name == "collectClassName") {
                    collectClassName = value.toString()
                }
                if (name == "invokeClassName") {
                    invokeClassName = value.toString()
                }
                if (name == "invokeMethod") {
                    invokeMethod = value.toString()
                }
                if (name == "isClazz") {
                    isClazz = value.toString()
                }
            }
            super.visit(name, value)
        }

        override fun visitEnd() {
            super.visitEnd()
            if (collectClassName != null && invokeClassName != null && invokeMethod != null) {
                addCollectConfig(AopCollectCut(collectClassName!!, invokeClassName!!, invokeMethod!!,isClazz == "true"))
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
                } else if (descriptor.contains(COLLECT_POINT)) {
                    CollectClassVisitor()
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

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    companion object {
        const val CLASS_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopClass"
        const val METHOD_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopPointCut"
        const val MATCH_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopMatchClassMethod"
        const val REPLACE_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopReplaceMethod"
        const val EXTENDS_POINT =
            "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopModifyExtendsClass"
        const val COLLECT_POINT = "Lcom/flyjingfish/android_aop_annotation/aop_anno/AopCollectMethod"
    }
}