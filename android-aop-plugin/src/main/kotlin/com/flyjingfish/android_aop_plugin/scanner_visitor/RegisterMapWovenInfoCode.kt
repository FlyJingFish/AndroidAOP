package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.InputStream

class RegisterMapWovenInfoCode {

    fun execute(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor = MyClassVisitor(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    inner class MyClassVisitor(private val mClassVisitor: ClassVisitor)
        : ClassVisitor(Opcodes.ASM9, mClassVisitor) {

        override fun visitMethod(
            access: Int,
            name: String?,
            desc: String?,
            signature: String?,
            exception: Array<out String>?
        ): MethodVisitor {
            var mv = mClassVisitor.visitMethod(access, name, desc, signature, exception)
            if ("registerCreators" == name) {
                mv = MyCutMethodAdapter(mv, access, name, desc)
            }else if ("registerMatchCreators" == name) {
                mv = MyMatchMethodAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    inner class MyCutMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?)
        : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                val map: HashMap<String, String> = WovenInfoUtils.aopInstances
                if (map.isNotEmpty()) {
                    map.forEach { (key, value) ->
                        if (key.startsWith("@")){
                            val methodVisitor = mv

                            methodVisitor.visitLdcInsn(key)

                            methodVisitor.visitTypeInsn(NEW, value)
                            methodVisitor.visitInsn(DUP)
                            methodVisitor.visitMethodInsn(
                                INVOKESPECIAL,
                                value,
                                "<init>",
                                "()V",
                                false
                            )

                            methodVisitor.visitMethodInsn(
                                INVOKESTATIC,
                                Utils.dotToSlash(Utils.JoinAnnoCutUtils),
                                "registerCreator",
                                "(Ljava/lang/String;Lcom/flyjingfish/android_aop_annotation/base/BasePointCutCreator;)V",
                                false
                            )
                        }
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
        }
    }

    inner class MyMatchMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?)
        : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                val map: HashMap<String, String> = WovenInfoUtils.aopInstances
                if (map.isNotEmpty()) {
                    map.forEach { (key, value) ->
                        if (!key.startsWith("@")){
                            val methodVisitor = mv

                            methodVisitor.visitLdcInsn(key)

                            methodVisitor.visitTypeInsn(NEW, value)
                            methodVisitor.visitInsn(DUP)
                            methodVisitor.visitMethodInsn(
                                INVOKESPECIAL,
                                value,
                                "<init>",
                                "()V",
                                false
                            )

                            methodVisitor.visitMethodInsn(
                                INVOKESTATIC,
                                Utils.dotToSlash(Utils.JoinAnnoCutUtils),
                                "registerMatchCreator",
                                "(Ljava/lang/String;Lcom/flyjingfish/android_aop_annotation/base/MatchClassMethodCreator;)V",
                                false
                            )
                        }
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
        }
    }
}