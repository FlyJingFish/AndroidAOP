package com.flyjingfish.androidaop.testReplace;

import android.util.Log;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod;

@AndroidAopReplaceClass("com.flyjingfish.androidaop.testReplace.BaseBean")
public class ReplaceBaseBean2 {
    @AndroidAopReplaceMethod("<init>(int,int)")
    public static BaseBean getBaseBean(BaseBean testBean)  {
        Log.e("ReplaceBaseBean2","getBaseBean");
        return testBean;
    }

    @AndroidAopReplaceMethod("void test()")
    public static void test(BaseBean testBean){
        Log.e("ReplaceBaseBean2","test");
        testBean.test();
    }
}
