package com.flyjingfish.android_aop_core.listeners

fun interface OnRequestPermissionListener {
    fun onCall(isResult: Boolean)
}