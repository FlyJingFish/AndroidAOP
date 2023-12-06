package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.base.BasePointCut

abstract class ClickCut<T : Annotation> : BasePointCut<T> {
    private var sLastClickTime: Long = 0

    private var sLastClickViewId = 0

    fun isSingleClick(v: View, intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val viewId = v.id
        val timeD = time - sLastClickTime
        return if (timeD in 1 until intervalMillis && viewId == sLastClickViewId) {
            false
        } else {
            sLastClickTime = time
            sLastClickViewId = viewId
            true
        }
    }

    fun isSingleClick(intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - sLastClickTime
        return if (timeD in 1 until intervalMillis) {
            false
        } else {
            sLastClickTime = time
            true
        }
    }

    fun isDoubleClick(v: View, intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val viewId = v.id
        val timeD = time - sLastClickTime
        return if (timeD in 1 until intervalMillis && viewId == sLastClickViewId) {
            sLastClickTime = 0
            sLastClickViewId = viewId
            true
        } else {
            sLastClickTime = time
            sLastClickViewId = viewId
            false
        }
    }

    fun isDoubleClick(intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - sLastClickTime
        return if (timeD in 1 until intervalMillis) {
            sLastClickTime = 0
            true
        } else {
            sLastClickTime = time
            false
        }
    }
}