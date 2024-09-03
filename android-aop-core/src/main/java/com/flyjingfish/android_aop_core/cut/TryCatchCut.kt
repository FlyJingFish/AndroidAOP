package com.flyjingfish.android_aop_core.cut

import android.text.TextUtils
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.TryCatch
import com.flyjingfish.android_aop_core.utils.AndroidAop
import com.flyjingfish.android_aop_core.utils.Utils

internal class TryCatchCut : BasePointCut<TryCatch> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: TryCatch): Any? {
        return try {
            joinPoint.proceed()
        } catch (e: Throwable) {
            var flag: String = anno.value
            if (TextUtils.isEmpty(flag)) {
                flag = Utils.getMethodName(joinPoint)
            }
            AndroidAop.getOnThrowableListener()?.handleThrowable(flag, e,anno)
        }
    }
}