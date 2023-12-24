package com.flyjingfish.test_lib;


import com.flyjingfish.android_aop_core.annotations.SingleClick;

public class StaticClass2 {
    @SingleClick(5000)
    public static void onStaticPermission(){

        ToastUtils.INSTANCE.makeText(ToastUtils.INSTANCE.getApp(), "StaticClass-onStaticPermission1");
    }

}
