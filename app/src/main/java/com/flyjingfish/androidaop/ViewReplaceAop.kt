package com.flyjingfish.androidaop

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopReplaceClass("android.view.View", type = MatchType.EXTENDS)
object ViewReplaceAop {

    @AndroidAopReplaceMethod("<init>(android.content.Context)")
    @JvmStatic
    fun newViewConstruction1(
        context: Context,
        clazz: Class<*>
    ): View {
        return clazz.getConstructor(Context::class.java).newInstance(context) as View
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet)")
    @JvmStatic
    fun newViewConstruction2(
        context: Context,
        attrs: AttributeSet?,
        clazz: Class<*>,
    ): View {
        //最后一个参数是Class类型其余参数类型及顺序和原构造方法完全一致，在这个方法内再去创建对象，此前并没有对象被创建出来
        return clazz.getConstructor(Context::class.java,AttributeSet::class.java).newInstance(context,attrs) as View
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet,int)")
    @JvmStatic
    fun newViewConstruction3(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        clazz: Class<*>
    ): View {
        //最后一个参数是Class类型其余参数类型及顺序和原构造方法完全一致，在这个方法内再去创建对象，此前并没有对象被创建出来
        return clazz.getConstructor(Context::class.java,AttributeSet::class.java,Int::class.java).newInstance(context,attrs,defStyleAttr) as View
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet,int,int)")
    @JvmStatic
    fun newViewConstruction4(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
        clazz: Class<*>
    ): View {
        Log.d(
            "ViewReplaceAop",
            "newViewConstruction4: $clazz,$context,$attrs,$defStyleAttr,$defStyleRes"
        )
        //最后一个参数是Class类型其余参数类型及顺序和原构造方法完全一致，在这个方法内再去创建对象，此前并没有对象被创建出来
        return clazz.getConstructor(Context::class.java,AttributeSet::class.java,Int::class.java,Int::class.java).newInstance(context,attrs,defStyleAttr,defStyleRes) as View
    }
}