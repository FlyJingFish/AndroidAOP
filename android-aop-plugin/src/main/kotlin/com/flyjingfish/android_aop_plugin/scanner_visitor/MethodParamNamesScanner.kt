package com.flyjingfish.android_aop_plugin.scanner_visitor

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodNode
import kotlin.math.min


class MethodParamNamesScanner(inputStreamBytes: ByteArray) {
    private val methods: List<MethodNode>

    init {
        val cr = ClassReader(inputStreamBytes)
        val cn = ClassNode()
        cr.accept(cn, ClassReader.EXPAND_FRAMES)
        val methods = cn.methods
        this.methods = methods
    }

    /**
     * 获取参数名列表辅助方法
     *
     * @param name
     * @param desc
     * @param size
     * @return
     */
    fun getParamNames(
        name: String,
        desc: String,
        size: Int
    ): List<String> {
        val list: MutableList<String> = ArrayList()
        for (i in methods.indices) {
            val varNames = mutableMapOf<Int,LocalVariable>()
            val method = methods[i]
            if (method.desc == desc && method.name == name) {
                val localVariables = method.localVariables ?: continue
                for (l in localVariables.indices) {
                    val varName = localVariables[l].name
                    // index-记录了正确的方法本地变量索引。(方法本地变量顺序可能会被打乱。而index记录了原始的顺序)
                    val index = localVariables[l].index
                    if ("this" != varName)
                        varNames[index] = (LocalVariable(index, varName))
                }

                val sortedMap = varNames.entries.sortedBy { it.key }.associate { it.toPair() }

                val tmpArr = mutableListOf<LocalVariable>()
                for (entry in sortedMap.entries) {
                    tmpArr.add(entry.value)
                }

                val len = min(tmpArr.size , size)
                for (j in 0 until len) {
                    list.add(tmpArr[j].name)
                }
                break
            }
        }
        if (size > 0 && list.isEmpty()) {
            for (i in 0 until size) {
                list.add("var$i")
            }
        }
        return list
    }

    fun getParamInfo(
        name: String,
        desc: String,
        size: Int
    ): List<LocalVariableNode> {
        val list: MutableList<LocalVariableNode> = ArrayList()
        for (i in methods.indices) {
            val varNames = mutableMapOf<Int,LocalNode>()
            val method = methods[i]
            if (method.desc == desc && method.name == name) {
                val localVariables = method.localVariables ?: continue
                for (l in localVariables.indices) {
                    val varName = localVariables[l].name
                    // index-记录了正确的方法本地变量索引。(方法本地变量顺序可能会被打乱。而index记录了原始的顺序)
                    val index = localVariables[l].index
                    if ("this" != varName)
                        varNames[index] = (LocalNode(index, localVariables[l]))
                }

                val sortedMap = varNames.entries.sortedBy { it.key }.associate { it.toPair() }

                val tmpArr = mutableListOf<LocalNode>()
                for (entry in sortedMap.entries) {
                    tmpArr.add(entry.value)
                }

                val len = min(tmpArr.size , size)
                for (j in 0 until len) {
                    list.add(tmpArr[j].variableNode)
                }
                break
            }
        }
        return list
    }

    fun getMethodNode(
        name: String,
        desc: String
    ): MethodNode?{
        for (i in methods.indices) {
            val method = methods[i]
            if (method.desc == desc && method.name == name) {
                return method
            }
        }
        return null
    }

    /**
     * 方法本地变量索引和参数名封装
     * @author xby Administrator
     */
    private class LocalVariable(var index: Int, var name: String) :
        Comparable<LocalVariable> {
        override fun compareTo(other: LocalVariable): Int {
            return index - other.index
        }
    }

    private class LocalNode(var index: Int, var variableNode: LocalVariableNode) :
        Comparable<LocalVariable> {
        override fun compareTo(other: LocalVariable): Int {
            return index - other.index
        }
    }

}