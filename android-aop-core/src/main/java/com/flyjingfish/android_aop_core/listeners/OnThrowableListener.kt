package com.flyjingfish.android_aop_core.listeners

import com.flyjingfish.android_aop_core.annotations.TryCatch

fun interface OnThrowableListener {
    fun handleThrowable(flag: String, throwable: Throwable?,tryCatch: TryCatch): Any?
}