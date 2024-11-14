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
        classKey:String,
        clazz: Class<*>,
        originalMethodName: String,
        targetMethodName: String,
        lambda: Boolean
    ) :AndroidAopJoinPoint{
        var obj = mJoinPointPool[classKey]
        if (obj == null){
            obj = AndroidAopJoinPoint(classKey,clazz, originalMethodName, targetMethodName, lambda)
            val oldInstance = mJoinPointPool.putIfAbsent(classKey, obj)
            if (oldInstance != null){
                obj = oldInstance
            }
        }else{
            removeWeaklyReachableObjectsOnIOThread()
        }
        return obj
    }

    fun observeTarget(target : Any?,key :String){
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
                val key = ref.key
                val classKey = key.split("@")[0]
                mTargetReferenceMap.remove(ref.key)
                var isHasClass = false
                val searchKey = "$classKey@"
                mTargetReferenceMap.forEach { (key, _) ->
                    if (key.startsWith(searchKey)){
                        isHasClass = true
                        return@forEach
                    }
                }
                if (!isHasClass){
                    mJoinPointPool.remove(classKey)
                }
            }
        } while (ref != null)
    }
}