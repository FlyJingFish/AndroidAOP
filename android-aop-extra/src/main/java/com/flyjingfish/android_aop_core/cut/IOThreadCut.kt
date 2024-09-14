package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.IOThread
import com.flyjingfish.android_aop_core.enums.ThreadType
import com.flyjingfish.android_aop_core.utils.AppExecutors

internal class IOThreadCut : BasePointCut<IOThread> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: IOThread): Any? {
        return if (Looper.getMainLooper() != Looper.myLooper()){
            joinPoint.proceed()
        }else{
            when (anno.value) {
                ThreadType.SingleIO, ThreadType.DiskIO -> AppExecutors.singleIO().execute {
                    getProceedResult(
                        joinPoint
                    )
                }

                ThreadType.MultipleIO, ThreadType.NetworkIO -> AppExecutors.poolIO()
                    .execute { getProceedResult(joinPoint) }
            }
            null
        }
    }

    private fun getProceedResult(joinPoint: ProceedJoinPoint): Any? {
        try {
            return joinPoint.proceed()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }
}