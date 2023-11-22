package com.flyjingfish.androidaop;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.flyjingfish.test_lib.annotation.MyAnno;

public class SecondActivity extends BaseActivity2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @MyAnno
    private void onSingleClick(){
        Log.e("Test_click","onSingleClick");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
