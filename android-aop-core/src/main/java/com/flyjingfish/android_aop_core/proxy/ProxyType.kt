package com.flyjingfish.android_aop_core.proxy

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceNew

enum class ProxyType {
    /**
     * [AndroidAopReplaceMethod] 注解的替换的方法是类的静态方法
     */
    STATIC_METHOD,

    /**
     * [AndroidAopReplaceMethod] 注解的替换的方法是类的成员方法
     */
    METHOD,

    /**
     * [AndroidAopReplaceNew] 注解的替换的方法，目前不适配此类型
     */
    NEW,

    /**
     * [AndroidAopReplaceMethod] 注解的替换的方法是 <init>，目前不适配此类型
     */
    INIT
}