package com.flyjingfish.android_aop_core.cut

import android.os.Looper
import com.flyjingfish.android_aop_annotation.BasePointCut
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.IOThread
import com.flyjingfish.android_aop_core.enums.ThreadType
import com.flyjingfish.android_aop_core.utils.AppExecutors

class IOThreadCut : BasePointCut<IOThread> {
    override fun invoke(joinPoint: ProceedJoinPoint, ioThread: IOThread): Any? {
        if (Looper.getMainLooper() != Looper.myLooper()){
            return joinPoint.proceed()
        }else{
//            val result: Any? = when (ioThread.value) {
//                ThreadType.Single, ThreadType.Disk -> AppExecutors.singleIO().submit(
//                    Callable<Any?> { getProceedResult(joinPoint) }).get()
//
//                ThreadType.Fixed, ThreadType.Network -> AppExecutors.poolIO()
//                    .submit(Callable<Any?> { getProceedResult(joinPoint) }).get()
//            }
            when (ioThread.value) {
                ThreadType.SingleIO, ThreadType.DiskIO -> AppExecutors.singleIO().execute {
                    getProceedResult(
                        joinPoint
                    )
                }

                ThreadType.MultipleIO, ThreadType.NetworkIO -> AppExecutors.poolIO()
                    .execute { getProceedResult(joinPoint) }
            }
            return null
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