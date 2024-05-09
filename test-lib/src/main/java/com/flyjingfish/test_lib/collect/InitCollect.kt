package com.flyjingfish.test_lib.collect

import android.app.Application
import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod
import com.flyjingfish.test_lib.SubApplication

object InitCollect {
    private val Collects = mutableListOf<SubApplication>()

    @AndroidAopCollectMethod
    @JvmStatic
    fun collect(sub: SubApplication):Unit{
        Collects.add(sub)
    }

    fun init(application: Application){
        for (collect in Collects) {
            collect.onCreate(application)
        }
    }
}