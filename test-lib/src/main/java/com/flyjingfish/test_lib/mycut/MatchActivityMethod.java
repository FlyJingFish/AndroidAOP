package com.flyjingfish.test_lib.mycut;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_annotation.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.test_lib.ToastUtils;

import java.lang.reflect.InvocationTargetException;

//@AndroidAopMatchClassMethod(targetClassName = "androidx.appcompat.app.AppCompatActivity",methodName = {"startActivity"})
public class MatchActivityMethod implements MatchClassMethod {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull String methodName) {
        Log.e("MatchActivityMethod","=====invoke====="+methodName);
        if (joinPoint.target instanceof Activity){
            ToastUtils.INSTANCE.makeText((Context) joinPoint.target,"进入匹配类切面");
        }
        return joinPoint.proceed();
    }
}
