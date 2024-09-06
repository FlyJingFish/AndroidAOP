package com.flyjingfish.android_aop_core.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import com.flyjingfish.android_aop_core.AndroidAopContentProvider

internal object DebugUtils {
    private var init = false
    private var debug = false

    fun isDebug(): Boolean {
        return if (init) {
            debug
        } else {
            val debugValue = isApkInDebug()
            debug = debugValue
            init = true
            debugValue
        }
    }

    private fun isApkInDebug(): Boolean {
        val context: Context = AndroidAopContentProvider.getAppContext()
        return try {
            val info = context.applicationInfo
            info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (_: Exception) {
            false
        }
    }
}