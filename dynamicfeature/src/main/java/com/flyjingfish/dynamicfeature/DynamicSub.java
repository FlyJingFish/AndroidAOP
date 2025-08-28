package com.flyjingfish.dynamicfeature;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.flyjingfish.test_lib.SubApplication;
import com.flyjingfish.test_lib.ToastUtils;

public class DynamicSub implements SubApplication {
    @Override
    public void onCreate(@NonNull Application application) {
        Log.e("DynamicSub","--onCreate---");
        ToastUtils.INSTANCE.makeText("onCreate-DynamicSub");
    }
}
