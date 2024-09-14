package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_annotation.base.BasePointCutSuspend

internal abstract class ClickCut<T : Annotation> : BasePointCutSuspend<T> {
    private var mLastClickTime: Long = 0

    private var mLastClickViewId = 0

    fun isSingleClick(v: View, intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val viewId = v.id
        val timeD = time - mLastClickTime
        return if (timeD in 1 until intervalMillis && viewId == mLastClickViewId) {
            false
        } else {
            mLastClickTime = time
            mLastClickViewId = viewId
            true
        }
    }

    fun isSingleClick(intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - mLastClickTime
        return if (timeD in 1 until intervalMillis) {
            false
        } else {
            mLastClickTime = time
            true
        }
    }

    fun isDoubleClick(v: View, intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val viewId = v.id
        val timeD = time - mLastClickTime
        return if (timeD in 1 until intervalMillis && viewId == mLastClickViewId) {
            mLastClickTime = 0
            mLastClickViewId = viewId
            true
        } else {
            mLastClickTime = time
            mLastClickViewId = viewId
            false
        }
    }

    fun isDoubleClick(intervalMillis: Long): Boolean {
        val time = System.currentTimeMillis()
        val timeD = time - mLastClickTime
        return if (timeD in 1 until intervalMillis) {
            mLastClickTime = 0
            true
        } else {
            mLastClickTime = time
            false
        }
    }
}