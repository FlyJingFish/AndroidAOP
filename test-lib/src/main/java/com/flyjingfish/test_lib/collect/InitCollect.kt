package com.flyjingfish.test_lib.collect

import android.app.Application
import android.util.Log
import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod
import com.flyjingfish.test_lib.SubApplication
import com.flyjingfish.test_lib.annotation.MyAnno

object InitCollect {
    private val Collects = mutableListOf<SubApplication>()

    @AndroidAopCollectMethod
    @JvmStatic
    fun collect(sub: SubApplication):Unit{
        Collects.add(sub)
    }

    @MyAnno
    fun init(application: Application){
        Log.e("InitCollect","----init----");
        for (collect in Collects) {
            collect.onCreate(application)
        }
    }
}