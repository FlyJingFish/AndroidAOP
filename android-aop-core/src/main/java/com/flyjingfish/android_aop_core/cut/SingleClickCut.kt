package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.SingleClick

internal class SingleClickCut : ClickCut<SingleClick>() {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: SingleClick): Any? {
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
            if (isSingleClick(targetView, anno.value)) {
                return joinPoint.proceed()
            }
        }else{
            if (isSingleClick(anno.value)) {
                return joinPoint.proceed()
            }
        }
        return null
    }

}