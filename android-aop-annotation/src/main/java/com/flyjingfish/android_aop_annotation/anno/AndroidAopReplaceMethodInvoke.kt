package com.flyjingfish.android_aop_annotation.anno

/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AndroidAopReplaceMethodInvoke(
    /**
     * @return 目标类名（包含包名）
     */
    val targetClassName: String,
    /**
     * @return 替换类名（包含包名）
     */
    val invokeClassName: String,
)
