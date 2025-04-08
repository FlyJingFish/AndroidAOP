package com.flyjingfish.test_lib.collect;

import android.app.Application;
import android.util.Log;
import android.view.View;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod;
import com.flyjingfish.android_aop_annotation.enums.CollectType;
import com.flyjingfish.test_lib.SubApplication2;
import com.flyjingfish.test_lib.SubApplication3;
import com.flyjingfish.test_lib.annotation.MyAnno;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.JvmStatic;
import kotlin.reflect.KClass;

public class InitCollect2 {
    private static final List<SubApplication2> collects = new ArrayList<>();
    private static final List<Class<? extends SubApplication2>> collectClazz = new ArrayList<>();
    @AndroidAopCollectMethod
    public static void collect(SubApplication2 sub){
        collects.add(sub);
    }

    @AndroidAopCollectMethod
    public static void collect3(Class<? extends SubApplication2> sub){
        collectClazz.add(sub);
    }

    @AndroidAopCollectMethod(collectType = CollectType.LEAF_EXTENDS)
    public static void collect3Leaf(Class<? extends SubApplication2> sub){
        Log.e("InitCollect2","----collect3Leaf----"+sub);
    }

    @AndroidAopCollectMethod(collectType = CollectType.LEAF_EXTENDS)
    public static void collect3Leaf(SubApplication2 sub){
        Log.e("InitCollect2","----collect3Leaf----"+sub);
    }


    @AndroidAopCollectMethod
    public static void collectT(SubApplication3<? extends Application> sub){
        Log.e("InitCollect2","----collectT----"+sub);
    }

    @AndroidAopCollectMethod
    public static void collectClassT(Class<? extends SubApplication3<? extends Application>> sub){
        Log.e("InitCollect2","----collectClassT----"+sub);
    }


    @AndroidAopCollectMethod(regex = ".*?\\$\\$AndroidAopClass")
    public static void collectAndroidAopClassRegex(Object sub){
        Log.e("InitCollect2","----collectAndroidAopClassRegexObject----"+sub);
    }

    @AndroidAopCollectMethod(regex = ".*?\\$\\$AndroidAopClass")
    public static void collectAndroidAopClassRegex(Class<?> sub){
        Log.e("InitCollect2","----collectAndroidAopClassRegexClazz----"+sub);
    }

//    @AndroidAopCollectMethod
//    public static void collectOnClickListener(Class<? extends View.OnClickListener> listener){
//    }

    @MyAnno
    public static void init(Application application){
        Log.e("InitCollect2","----init----");
        for (SubApplication2 collect : collects) {
            collect.onCreate(application);
        }

        for (Class<? extends SubApplication2> sub : collectClazz) {
            Log.e("InitCollect2","----init----sub="+sub);
        }
    }
}
