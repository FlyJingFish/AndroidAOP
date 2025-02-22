package com.flyjingfish.androidaop;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.flyjingfish.test_lib.BaseActivity;

public class BaseVActivity2<VB extends ViewBinding> extends BaseVActivity<VB> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
}
