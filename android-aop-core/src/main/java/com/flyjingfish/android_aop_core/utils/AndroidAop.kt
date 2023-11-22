package com.flyjingfish.android_aop_core.utils

import com.flyjingfish.android_aop_core.listeners.OnCustomInterceptListener
import com.flyjingfish.android_aop_core.listeners.OnPermissionsInterceptListener
import com.flyjingfish.android_aop_core.listeners.OnThrowableListener

object AndroidAop {
    private var onThrowableListener: OnThrowableListener? = null
    private var onCustomInterceptListener: OnCustomInterceptListener? = null
    private var onPermissionsInterceptListener: OnPermissionsInterceptListener? = null

    /**
     * 如果你使用了 [com.flyjingfish.android_aop_core.annotations.TryCatch], 设置此项可以拿到异常回调
     */
    fun setOnThrowableListener(listener: OnThrowableListener) {
        onThrowableListener = listener
    }

    fun getOnThrowableListener(): OnThrowableListener? {
        return onThrowableListener
    }

    /**
     * 如果你使用了 [com.flyjingfish.android_aop_core.annotations.CustomIntercept] ,设置此项可以拿到自定义拦截回调
     */
    fun setOnCustomInterceptListener(listener: OnCustomInterceptListener) {
        onCustomInterceptListener = listener
    }

    fun getOnCustomInterceptListener(): OnCustomInterceptListener? {
        return onCustomInterceptListener
    }

    /**
     * 如果你使用了 [com.flyjingfish.android_aop_core.annotations.Permission], 设置此项才可以请求权限
     */
    fun setOnPermissionsInterceptListener(listener: OnPermissionsInterceptListener) {
        onPermissionsInterceptListener = listener
    }

    fun getOnPermissionsInterceptListener(): OnPermissionsInterceptListener? {
        return onPermissionsInterceptListener
    }


}