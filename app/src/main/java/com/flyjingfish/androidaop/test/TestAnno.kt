package com.flyjingfish.androidaop.test

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.test_lib.mycut.MyAnnoCut2

@AndroidAopPointCut(TestAnnoCut::class)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class TestAnno