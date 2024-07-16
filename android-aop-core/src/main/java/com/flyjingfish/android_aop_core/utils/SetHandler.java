package com.flyjingfish.android_aop_core.utils;

import android.os.Handler;
import android.os.Looper;

import com.flyjingfish.android_aop_annotation.utils.HandlerUtils;

class SetHandler {
    static {
        Handler handler = new Handler(Looper.getMainLooper());
        HandlerUtils.INSTANCE.setHandler(handler::post);
    }
}
