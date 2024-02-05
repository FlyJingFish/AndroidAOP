package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.AopMethodCut
import com.flyjingfish.android_aop_plugin.utils.Utils
import com.flyjingfish.android_aop_plugin.utils.WovenInfoUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.InputStream
import java.util.HashMap

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
            if ("registerMap" == name) {
                mv = MyMethodAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    inner class MyMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?)
        : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                val map: HashMap<String, AopMethodCut> = WovenInfoUtils.aopMethodCuts
                if (map.isNotEmpty()) {
                    map.forEach { (_, value) ->
                        val name = value.anno+"-"+value.cutClassName
                        mv.visitLdcInsn(name)
                        mv.visitMethodInsn(
                            INVOKESTATIC,
                            Utils.dotToSlash(Utils.MethodAnnoUtils),
                            "register",
                            "(Ljava/lang/String;)V",
                            false
                        )
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