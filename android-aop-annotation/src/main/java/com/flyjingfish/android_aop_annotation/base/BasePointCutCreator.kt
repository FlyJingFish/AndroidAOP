package com.flyjingfish.android_aop_annotation.base

interface BasePointCutCreator {
    fun newInstance(): BasePointCut<out Annotation>
}