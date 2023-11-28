package com.flyjingfish.androidaop.test2;


import com.flyjingfish.android_aop_core.annotations.SingleClick;
import com.flyjingfish.androidaop.MainActivity;
import com.flyjingfish.androidaop.ThirdActivity;

public class StaticClass {
    @SingleClick(5000)
    public static void onStaticPermission(MainActivity activity, int maxSelect , ThirdActivity.OnPhotoSelectListener back){
        back.onBack();
    }

}
