package com.flyjingfish.android_aop_core.cut

import android.view.View
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_core.annotations.SingleClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: SingleClick) {
        withContext(Dispatchers.Main) {
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
                    joinPoint.proceed()
                }else{
                    joinPoint.proceedIgnoreOther {
                        null
                    }
                }
            }else{
                if (isSingleClick(anno.value)) {
                    joinPoint.proceed()
                }else{
                    joinPoint.proceedIgnoreOther {
                        null
                    }
                }
            }
        }
    }

}