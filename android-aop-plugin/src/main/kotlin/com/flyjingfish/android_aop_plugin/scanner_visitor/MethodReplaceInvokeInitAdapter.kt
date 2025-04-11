package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.beans.ReplaceMethodInfo
import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode


class MethodReplaceInvokeInitAdapter(private val className:String, private val superName: String, private val methodAccess:Int,
                                     private val methodName:String, private val methodDesc:String, private val methodSignature: String?,
                                     private val methodExceptions: Array<String?>?, private val methodVisitor: MethodVisitor?, private val delNews:List<ReplaceMethodInfo>) :
    MethodNode(Opcodes.ASM9,methodAccess,methodName,methodDesc,methodSignature,methodExceptions){


    override fun visitEnd() {
        val deleteNews = mutableListOf<ReplaceMethodInfo>().apply {
            addAll(delNews)
        }
        val insnList = this.instructions
        val removeList = mutableListOf<AbstractInsnNode?>()
        val array = insnList.toArray()

        for (i in array.indices) {
            val insn = array[i]
            if (insn is MethodInsnNode) {
                val owner = insn.owner
                val name = insn.name
                val descriptor = insn.desc
                var relaceInfo: ReplaceMethodInfo?=null
                for (deleteNew in deleteNews) {
                    if (deleteNew.oldOwner == owner && deleteNew.oldMethodName == name && deleteNew.oldMethodDesc == descriptor){
                        relaceInfo = deleteNew
                        break
                    }
                }

                relaceInfo?:continue

                deleteNews.remove(relaceInfo)
                // 查找 new/dup
                val (newInsn, dupInsn) = findNewAndDup(insn,relaceInfo.oldOwner)
                    ?: continue

                removeList += listOf(newInsn, dupInsn)


                // 插入类对象
                instructions.insertBefore(insn, LdcInsnNode(Type.getObjectType(owner)))


                // 替换为静态方法
                val staticMethodInsn = MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    relaceInfo.newOwner,
                    relaceInfo.newMethodName,
                    relaceInfo.newMethodDesc,
                    false
                )
                instructions.set(insn, staticMethodInsn)

                val castInsn = TypeInsnNode(Opcodes.CHECKCAST, owner)
                instructions.insert(staticMethodInsn, castInsn)
            }
        }

        // 删除 new + dup
        removeList.forEach {
            instructions.remove(it)
        }

        // 写回 MethodVisitor
        accept(methodVisitor)
    }

    private fun findNewAndDup(initInsn: AbstractInsnNode,owner: String): Pair<AbstractInsnNode?, AbstractInsnNode?>? {
        var current = initInsn.previous
        var dup: AbstractInsnNode? = null
        var newInsn: AbstractInsnNode? = null
        while (current != null) {
            if (current.opcode == Opcodes.DUP) {
                dup = current
            } else if (current.opcode == Opcodes.NEW && current is TypeInsnNode && current.desc == owner) {
                newInsn = current
                break
            }
            current = current.previous
        }
        return if (newInsn != null && dup != null) Pair(newInsn, dup) else null
    }

}