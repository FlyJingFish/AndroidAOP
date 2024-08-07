package com.flyjingfish.android_aop_plugin.scanner_visitor

import com.flyjingfish.android_aop_plugin.utils.printLog
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.util.Arrays


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
            val varNames: MutableList<LocalVariable> = ArrayList()
            val method = methods[i]
            if (method.desc == desc && method.name == name) {
                val localVariables = method.localVariables
                for (l in localVariables.indices) {
                    val varName = localVariables[l].name
                    // index-记录了正确的方法本地变量索引。(方法本地变量顺序可能会被打乱。而index记录了原始的顺序)
                    val index = localVariables[l].index
                    if ("this" != varName)
                        varNames.add(LocalVariable(index, varName))
                }
                val tmpArr = varNames.toTypedArray()
                // 根据index来重排序，以确保正确的顺序
                Arrays.sort(tmpArr)
                for (j in 0 until size) {
                    list.add(tmpArr[j].name)
                }
                break
            }
        }
        return list
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

}