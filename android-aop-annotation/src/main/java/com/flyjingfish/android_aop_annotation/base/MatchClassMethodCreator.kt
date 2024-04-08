package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod

/**
 * 匹配切面的回调接口与 [AndroidAopMatchClassMethod] 配合使用
 */
interface MatchClassMethodCreator {
    fun newInstance(): MatchClassMethod
}