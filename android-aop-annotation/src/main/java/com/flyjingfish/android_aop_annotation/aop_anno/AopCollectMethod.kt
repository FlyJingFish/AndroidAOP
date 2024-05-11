package com.flyjingfish.android_aop_annotation.aop_anno

/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AopCollectMethod(
    /**
     * @return 需要收集的类名（包含包名）
     */
    val collectClassName: String,
    /**
     * @return 执行类名（包含包名）
     */
    val invokeClassName: String,
    /**
     * @return 执行方法
     */
    val invokeMethod: String,
    /**
     * @return 是否是 class
     */
    val isClazz:String = "false"
)
