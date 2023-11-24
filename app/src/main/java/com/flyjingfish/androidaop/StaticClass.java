package com.flyjingfish.androidaop;


import com.flyjingfish.android_aop_core.annotations.SingleClick;

public class StaticClass {
    @SingleClick(5000)
    public static void onStaticPermission(MainActivity activity, int maxSelect , ThirdActivity.OnPhotoSelectListener back){
        back.onBack();
    }

}
