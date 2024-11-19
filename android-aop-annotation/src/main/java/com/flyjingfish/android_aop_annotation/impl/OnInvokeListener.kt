package com.flyjingfish.android_aop_annotation.impl

internal fun interface OnInvokeListener {
    @Throws(Throwable::class)
    fun onInvoke(): Any?
}