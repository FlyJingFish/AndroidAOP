package com.flyjingfish.android_aop_core.listeners

import android.content.Context

fun interface OnToastListener {
    fun onToast(context: Context, text: CharSequence, duration: Int)
}