package com.flyjingfish.test_lib.annotation

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

@AndroidAopPointCut(MyAnnoCut3::class)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class MyAnno3