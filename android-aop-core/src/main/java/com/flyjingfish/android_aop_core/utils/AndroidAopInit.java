package com.flyjingfish.android_aop_core.utils;

import android.os.Handler;
import android.os.Looper;


import com.flyjingfish.android_aop_annotation.utils.AndroidAOPDebugUtils;
import com.flyjingfish.android_aop_annotation.utils.HandlerUtils;

class AndroidAopInit {
    static {
        Handler handler = new Handler(Looper.getMainLooper());
        HandlerUtils.INSTANCE.setHandler(runnable -> {
            if (Looper.myLooper() == Looper.getMainLooper()){
                runnable.run();
            }else {
                handler.post(runnable);
            }
        });
        AndroidAOPDebugUtils.INSTANCE.setDebug(DebugUtils.INSTANCE.isDebug());
    }
}
