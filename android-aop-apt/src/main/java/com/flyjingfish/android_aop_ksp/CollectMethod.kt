package com.flyjingfish.android_aop_ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

data class CollectMethod(
    val symbol : KSFunctionDeclaration,
    /**
     * 需要收集的类名（包含包名）
     */
    val collectClassName: String,
    /**
     * 执行类名（包含包名）
     */
    val invokeClassName: String,
    /**
     * 执行方法
     */
    val invokeMethod: String,
    /**
     * 是否是 class
     */
    val isClazz: String = "false",
    /**
     * 正则表达式
     */
    val regex: String = "",
    /**
     * 收集的类型
     */
    val collectType: String
)