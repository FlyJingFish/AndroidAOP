package com.flyjingfish.test_lib;


import android.util.Log;

import com.flyjingfish.test_lib.TestMatch;

public class TestMatch3 {
    public int label = 0;

    public TestMatch3() {
    }

    public TestMatch3(int label) {
        this.label = label;
    }

    public void test1(){
        Log.e("test1","label="+label);
    }
}
