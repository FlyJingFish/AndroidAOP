package com.flyjingfish.androidaop;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_core.annotations.CustomIntercept;
import com.flyjingfish.android_aop_core.annotations.Permission;
import com.flyjingfish.android_aop_core.listeners.OnCustomInterceptListener;
import com.flyjingfish.android_aop_core.listeners.OnPermissionsInterceptListener;
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener;
import com.flyjingfish.android_aop_core.listeners.OnThrowableListener;
import com.flyjingfish.android_aop_core.utils.AndroidAop;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidAop.INSTANCE.setOnPermissionsInterceptListener(new OnPermissionsInterceptListener() {
            @SuppressLint("CheckResult")
            @Override
            public void requestPermission(@NonNull ProceedJoinPoint joinPoint, @NonNull Permission permission, @NonNull OnRequestPermissionListener call) {
                Object target = joinPoint.getTarget();
                Log.e("requestPermission",""+permission.value());
//                if (target instanceof FragmentActivity){
//                    RxPermissions rxPermissions = new RxPermissions((FragmentActivity) target);
//                    rxPermissions.request(permission.value()).subscribe(call::onCall);
//                }else if (target instanceof Fragment){
//                    RxPermissions rxPermissions = new RxPermissions((Fragment) target);
//                    rxPermissions.request(permission.value()).subscribe(call::onCall);
//                }
            }
        });

        AndroidAop.INSTANCE.setOnCustomInterceptListener(new OnCustomInterceptListener() {
            @Nullable
            @Override
            public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CustomIntercept customIntercept) {
                // TODO: 2023/11/11 在此写你的逻辑 在合适的地方调用 joinPoint.proceed()，
                //  joinPoint.proceed(args)可以修改方法传入的参数，如果需要改写返回值，则在 return 处返回即可
                //  不调用 proceed 就不会执行拦截切面方法内的代码
                Log.e("CustomIntercept","invoke"+(customIntercept == null));
                return null;
            }
        });

        AndroidAop.INSTANCE.setOnThrowableListener(new OnThrowableListener() {
            @Nullable
            @Override
            public Object handleThrowable(@NonNull String flag, @Nullable Throwable throwable) {
                // TODO: 2023/11/11 发生异常可根据你当时传入的flag作出相应处理，如果需要改写返回值，则在 return 处返回即可
                Log.e("ThrowableListener","handleThrowable");
                return 3;
            }
        });
    }
}
