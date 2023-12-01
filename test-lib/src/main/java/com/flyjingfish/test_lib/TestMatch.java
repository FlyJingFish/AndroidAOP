package com.flyjingfish.test_lib;


import android.util.Log;
import android.view.View;

import com.flyjingfish.android_aop_core.annotations.SingleClick;
import com.flyjingfish.test_lib.annotation.MyAnno;
import com.flyjingfish.test_lib.annotation.MyAnno2;

public class TestMatch implements View.OnClickListener {
    public void test1(){

    }
    public void test2(){

    }
    @SingleClick(5000)
    public void test1(int value1,String value2){

    }
    @SingleClick(5000)
    @MyAnno
    public String test2(int value1,String value2){
        Log.e("TestMatch","====test2====");
        return value1+value2;
    }

    @Override
    public void onClick(View v) {

    }
}
