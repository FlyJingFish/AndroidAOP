package com.flyjingfish.test_lib.collect;

import android.app.Application;
import android.util.Log;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopCollectMethod;
import com.flyjingfish.test_lib.SubApplication2;
import com.flyjingfish.test_lib.annotation.MyAnno;

import java.util.ArrayList;
import java.util.List;

public class InitCollect2 {
    private static List<SubApplication2> collects = new ArrayList<>();
    private static List<Class<? extends SubApplication2>> collectClazz = new ArrayList<>();
    @AndroidAopCollectMethod
    public static void collect(SubApplication2 sub){
        collects.add(sub);
    }

    @AndroidAopCollectMethod
    public static void collect2(SubApplication2 sub){
        collects.add(sub);
    }

    @AndroidAopCollectMethod
    public static void collect3(Class<? extends SubApplication2> sub){
        collectClazz.add(sub);
    }

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
