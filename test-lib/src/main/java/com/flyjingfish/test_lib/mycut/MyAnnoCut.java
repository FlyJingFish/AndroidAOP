package com.flyjingfish.test_lib.mycut;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.flyjingfish.android_aop_core.utils.AppExecutors;
import com.flyjingfish.test_lib.ToastUtils;
import com.flyjingfish.test_lib.annotation.MyAnno;

public class MyAnnoCut implements BasePointCut<MyAnno> {
    @Nullable
    @Override
    public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull MyAnno anno) {
        AppExecutors.INSTANCE.poolIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                joinPoint.proceed();
            }
        });
        ToastUtils.INSTANCE.makeText(ToastUtils.app,"进入MyAnnoCut切面");
        return null;
    }
}
