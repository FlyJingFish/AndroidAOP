package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.DoubleClick

internal class DoubleClickCut : ClickCut<DoubleClick>() {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: DoubleClick): Any? {
        var view: View? = null
        joinPoint.args?.let {
            for (arg in it) {
                if (arg is View) {
                    view = arg
                    break
                }
            }
        }
        val targetView = view
        if (targetView != null) {
            if (isDoubleClick(targetView, anno.value)) {
                joinPoint.proceed()
            }
        }else{
            if (isDoubleClick(anno.value)) {
                joinPoint.proceed()
            }
        }
        return null
    }

}