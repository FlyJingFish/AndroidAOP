package com.flyjingfish.android_aop_annotation.enums

import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod

/**
 * [wiki 文档匹配类型说明](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopCollectMethod#%E7%AE%80%E8%BF%B0)
 */
enum class CollectType {
    /**
     * 表示匹配的是继承自
     *  - [AndroidAopCollectMethod] 注解的函数的参数类型
     *
     *  设置的类，包含 [DIRECT_EXTENDS],[LEAF_EXTENDS]
     */
    EXTENDS,

    /**
     * 表示的匹配的是 ***直接继承*** 自
     *  - [AndroidAopCollectMethod] 注解的函数的参数类型
     *
     * 设置的类
     */
    DIRECT_EXTENDS,

    /**
     * 表示的匹配的是 ***末端继承（就是没有子类了，最终子类）*** 自
     *  - [AndroidAopCollectMethod] 注解的函数的参数类型
     *
     * 设置的类 默认不扫描 kotlin/ 和kotlinx/ 这两个包下的代码
     */
    LEAF_EXTENDS
}