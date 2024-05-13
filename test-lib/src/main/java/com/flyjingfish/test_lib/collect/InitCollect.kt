package com.flyjingfish.test_lib.collect

import android.app.Application
import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod
import com.flyjingfish.test_lib.SubApplication
import com.flyjingfish.test_lib.SubApplication3

object InitCollect {
    private val Collects = mutableListOf<SubApplication>()
    private val collectClazz: MutableList<Class<out SubApplication>> = mutableListOf()

    @AndroidAopCollectMethod
    @JvmStatic
    fun collect(sub: SubApplication){
        Collects.add(sub)
    }

    @AndroidAopCollectMethod
    @JvmStatic
    fun collect2(sub:Class<out SubApplication>){
        collectClazz.add(sub)
    }

    @AndroidAopCollectMethod
    @JvmStatic
    fun collectT(sub: SubApplication3<out Application>){
        Log.e("InitCollect", "----collectT----$sub")
    }

    @AndroidAopCollectMethod
    @JvmStatic
    fun collectClassT(sub:Class<out SubApplication3<out Application>>){
        Log.e("InitCollect", "----collectClassT----$sub")
    }

    fun init(application: Application){
        Log.e("InitCollect","----init----");
        for (collect in Collects) {
            collect.onCreate(application)
        }
        for (sub in collectClazz) {
            Log.e("InitCollect", "----init----sub=$sub")
        }
    }
}