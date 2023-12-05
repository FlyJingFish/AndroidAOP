package com.flyjingfish.androidaop;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.flyjingfish.androidaop.databinding.ActivitySecondBinding;
import com.flyjingfish.androidaop.test.MyOnClickListener2;
import com.flyjingfish.test_lib.annotation.MyAnno;

public class SecondActivity extends BaseActivity2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySecondBinding binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        binding.btnInner.setOnClickListener(v -> onSingleClick());
        binding.btnInner.setOnClickListener((MyOnClickListener2) v -> onSingleClick());
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
