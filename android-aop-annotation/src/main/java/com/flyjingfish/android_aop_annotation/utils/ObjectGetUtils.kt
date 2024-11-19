package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.AndroidAopJoinPoint
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ObjectGetUtils {

    private val mJoinPointPool = ConcurrentHashMap<String, AndroidAopJoinPoint>()
    private val mSingleIO: ExecutorService = Executors.newSingleThreadExecutor()
    private val mTargetReferenceMap = ConcurrentHashMap<String, KeyWeakReference<Any>>()
    private val mTargetKeyReferenceQueue = ReferenceQueue<Any>()

    fun getAndroidAopJoinPoint(
        classMethodKey:String,
        clazz: Class<*>,
        target : Any?,
        originalMethodName: String,
        targetMethodName: String,
        lambda: Boolean
    ) :AndroidAopJoinPoint{
        val key = classMethodKey+System.identityHashCode(target)+Thread.currentThread().id
        var obj = mJoinPointPool[key]
        if (obj == null){
            obj = AndroidAopJoinPoint(classMethodKey,clazz, originalMethodName, targetMethodName, lambda)
            val oldInstance = mJoinPointPool.putIfAbsent(key, obj)
            if (oldInstance != null){
                obj = oldInstance
            }
            observeTarget(target, key)
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return obj
    }

    private fun observeTarget(target : Any?,key :String){
        mSingleIO.execute{
            if (target != null){
                val weakReference = KeyWeakReference(target, mTargetKeyReferenceQueue,key)
                mTargetReferenceMap[key] = weakReference
            }
            removeWeaklyReachableObjects()
        }
    }

    private fun removeWeaklyReachableObjectsOnIOThread(){
        mSingleIO.execute{
            removeWeaklyReachableObjects()
        }
    }

    private fun removeWeaklyReachableObjects() {
        var ref: KeyWeakReference<*>?
        do {
            ref = mTargetKeyReferenceQueue.poll() as KeyWeakReference<*>?
            if (ref != null) {
                mTargetReferenceMap.remove(ref.key)
                mJoinPointPool.remove(ref.key)
            }
        } while (ref != null)
    }
}