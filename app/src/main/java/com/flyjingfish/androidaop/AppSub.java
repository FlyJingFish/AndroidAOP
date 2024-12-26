package com.flyjingfish.androidaop;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.flyjingfish.test_lib.SubApplication;
import com.flyjingfish.test_lib.ToastUtils;

public class AppSub implements SubApplication {
    @Override
    public void onCreate(@NonNull Application application) {
        Log.e("AppSub","--onCreate---");
        ToastUtils.INSTANCE.makeText("onCreate-AppSub");
    }
}
