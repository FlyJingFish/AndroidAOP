package com.flyjingfish.androidaop;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.flyjingfish.test_lib.BaseActivity;

public class BaseVActivity<VB extends ViewBinding> extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    static <T> T getType(T vb){
        return vb;
    }
}
