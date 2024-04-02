package com.flyjingfish.android_aop_annotation.enums

import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass

/**
 * [wiki 文档匹配类型说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopMatchClassMethod#%E7%AE%80%E8%BF%B0)
 */
enum class MatchType {
    /**
     * 表示匹配的是继承自
     *  - [AndroidAopMatchClassMethod.targetClassName]
     *  - [AndroidAopReplaceClass.value]
     *
     *  设置的类，包含 [MatchType.DIRECT_EXTENDS],[MatchType.LEAF_EXTENDS]
     */
    EXTENDS,

    /**
     * 表示匹配的是
     *  - [AndroidAopMatchClassMethod.targetClassName]
     *  - [AndroidAopReplaceClass.value]
     *
     * 设置的类自身
     */
    SELF,

    /**
     * 表示的匹配的是 ***直接继承*** 自
     *  - [AndroidAopMatchClassMethod.targetClassName]
     *  - [AndroidAopReplaceClass.value]
     *
     * 设置的类
     */
    DIRECT_EXTENDS,

    /**
     * 表示的匹配的是 ***末端继承（就是没有子类了，最终子类）*** 自
     *  - [AndroidAopMatchClassMethod.targetClassName]
     *  - [AndroidAopReplaceClass.value]
     *
     * 设置的类 默认不扫描 kotlin/ 和kotlinx/ 这两个包下的代码
     */
    LEAF_EXTENDS
}