package com.flyjingfish.android_aop_plugin.config

open class AndroidAopConfig {
    /**
     * 是否启用，默认启用
     */
    var enabled = true
    /**
     * 是否开启debug模式，输出调试相关信息，以便排查问题，默认关闭
     */
    var debug = false
    /**
     * 是否开启验证叶子继承，默认打开，如果没有设置 AndroidAopMatchClassMethod 的 type = MatchType.LEAF_EXTENDS，可以关闭
     */
    var verifyLeafExtends = true
    /**
     * 包含规则
     */
    val includes = mutableListOf<String>()
    /**
     * 排除规则
     */
    val excludes = mutableListOf<String>()

    fun include(vararg filters: String): AndroidAopConfig {
        this.includes.addAll(filters)
        return this
    }

    fun exclude(vararg filters: String): AndroidAopConfig {
        this.excludes.addAll(filters)
        return this
    }

    companion object{
        var debug = false
        val includes = mutableListOf<String>()
        val excludes = mutableListOf<String>()
        var verifyLeafExtends = true
    }
}