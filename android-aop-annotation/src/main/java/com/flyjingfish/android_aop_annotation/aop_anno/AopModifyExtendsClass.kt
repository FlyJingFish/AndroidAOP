package com.flyjingfish.android_aop_annotation.aop_anno

/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AopModifyExtendsClass(
    /**
     * @return 目标类名（包含包名）
     */
    val targetClassName: String,
    /**
     * @return 替换类名（包含包名）
     */
    val extendsClassName: String,

    val isParent: Boolean = false,
    val weavingRules: String = ""
)
