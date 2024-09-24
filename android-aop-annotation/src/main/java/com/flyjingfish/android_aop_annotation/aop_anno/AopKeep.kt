package com.flyjingfish.android_aop_annotation.aop_anno


@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.TYPE,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.FIELD
)
annotation class AopKeep