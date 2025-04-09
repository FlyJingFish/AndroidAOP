package com.flyjingfish.androidaop.testReplace;

import android.util.Log;

public class BaseBean {
    int num1;
    int num2;

    public BaseBean(int num1, int num2) {
        this.num1 = num1;
        this.num2 = num2;
    }

    public BaseBean(int num1) {
        this(num1,0);
    }

    public void test(){
        Log.e("BaseBean","test");
    }
}
