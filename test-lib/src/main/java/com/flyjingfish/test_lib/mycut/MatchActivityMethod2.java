package com.flyjingfish.test_lib.mycut;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

@AndroidAopMatchClassMethod(
        targetClassName = "com.flyjingfish.test_lib.TestMatch",
        methodName = {"void test1(int, java.lang.String)","java.lang.String test2(int,java.lang.String)"},
        type = MatchType.SELF
)
public class MatchActivityMethod2 implements MatchClassMethod {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull String methodName) {
        Log.e("MatchActivityMethod2","======"+methodName+",getParameterTypes="+joinPoint.getTargetMethod().getParameterTypes().length);


        return joinPoint.proceed();
    }
}
