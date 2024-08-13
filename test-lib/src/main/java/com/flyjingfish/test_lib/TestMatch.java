package com.flyjingfish.test_lib;


import android.util.Log;

import com.flyjingfish.android_aop_core.annotations.SingleClick;
import com.flyjingfish.test_lib.annotation.MyAnno;

public class TestMatch{
    public int label = 0;

    public TestMatch() {
    }

    public TestMatch(int label) {
        this.label = label;
    }

    public void test1(){
        Log.e("test1","label="+label);
    }
    public void test2(){

    }
    public void test1(int value1){

    }
    @SingleClick(5000)
    public Object testhahhahahah1(Object value1,Object... value2){
        return new Object();
    }
    @SingleClick(5000)
    @MyAnno
    public String test2(int value1,String value2){
        Log.e("TestMatch","====test2====");
        return value1+value2;
    }

}
