package com.flyjingfish.androidaop.test2;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.flyjingfish.android_aop_core.annotations.OnLifecycle;
import com.flyjingfish.android_aop_core.annotations.SingleClick;
import com.flyjingfish.androidaop.MainActivity;
import com.flyjingfish.androidaop.ThirdActivity;

public class StaticClass {
    @SingleClick(5000)
    @OnLifecycle(Lifecycle.Event.ON_RESUME)
    public static void onStaticPermission(MainActivity activity, int maxSelect , ThirdActivity.OnPhotoSelectListener back){
        back.onBack();
    }

}
