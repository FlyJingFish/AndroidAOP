package com.flyjingfish.test_lib.collect

import android.app.Application
import android.util.Log
import android.view.View.OnClickListener
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
    fun collectT(sub: SubApplication3<*>){
        Log.e("InitCollect", "----collectT----$sub")
    }

    @AndroidAopCollectMethod
    @JvmStatic
    fun collectClassT(sub:Class<out SubApplication3<out Application>>){
        Log.e("InitCollect", "----collectClassT----$sub")
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$AndroidAopClass")
    @JvmStatic
    fun collectAndroidAopClassRegex(sub:Class<out Any>){
        Log.e("InitCollect", "----collectAndroidAopClassRegexClazz----$sub")
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\\$AndroidAopClass")
    @JvmStatic
    fun collectAndroidAopClassRegex(sub:Any){
        Log.e("InitCollect", "----collectAndroidAopClassRegexObject----$sub")
    }

    @AndroidAopCollectMethod
    @JvmStatic
    fun collectOnClickListener(listener: Class<out OnClickListener>){
    }
    fun init(application: Application){
        Log.e("InitCollect","----init----");
        for (collect in Collects) {
            Log.e("InitCollect", "----init----Collects=$collect")
            collect.onCreate(application)
        }
        for (sub in collectClazz) {
            Log.e("InitCollect", "----init----sub=$sub")
        }
    }

    @JvmStatic
    fun testEasyRegister(){

    }

    @JvmStatic
    fun addEasyRegister(classname:String){

    }
}