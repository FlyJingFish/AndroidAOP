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
    @AndroidAopCollectMethod
    public static void collect(SubApplication2 sub){
        collects.add(sub);
    }

    @MyAnno
    public static void init(Application application){
        Log.e("InitCollect2","----init----");
        for (SubApplication2 collect : collects) {
            collect.onCreate(application);
        }
    }
}
