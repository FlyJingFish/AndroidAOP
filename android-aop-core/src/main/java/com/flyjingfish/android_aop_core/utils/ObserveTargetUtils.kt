package com.flyjingfish.android_aop_core.utils

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal object ObserveTargetUtils {
    private val mTargetReferenceMap = ConcurrentHashMap<String, WeakReference<Any>>()
    private val mSingleIO: ExecutorService = Executors.newSingleThreadExecutor()

    fun observeTarget(joinPoint: ProceedJoinPoint,key :String){
        mSingleIO.execute{
            synchronized(ObserveTargetUtils){
                val target = joinPoint.target
                if (target != null){
                    val weakReference = WeakReference(target)
                    mTargetReferenceMap[key] = weakReference
                }
            }
//            removeWeaklyReachableObjects()
        }
    }

//    private fun removeWeaklyReachableObjectsOnIOThread(){
//        mSingleIO.execute{
//            removeWeaklyReachableObjects()
//        }
//    }
//
//    private fun removeWeaklyReachableObjects() {
//        synchronized(ObserveTargetUtils){
//            var ref: KeyWeakReference<*>?
//            do {
//                ref = mTargetKeyReferenceQueue.poll() as KeyWeakReference<*>?
//                if (ref != null) {
//                    mTargetReferenceMap.remove(ref.key)
//                }
//            } while (ref != null)
//        }
//    }
}