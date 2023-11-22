package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.DoubleClick

class DoubleClickCut : ClickCut<DoubleClick>() {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: DoubleClick): Any? {
        var view: View? = null
        for (arg in joinPoint.args) {
            if (arg is View) {
                view = arg
                break
            }
        }
        if (view != null) {
            if (isDoubleClick(view, anno.value)) {
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