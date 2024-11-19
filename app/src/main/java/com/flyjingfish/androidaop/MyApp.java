package com.flyjingfish.androidaop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_core.annotations.CustomIntercept;
import com.flyjingfish.android_aop_core.annotations.Permission;
import com.flyjingfish.android_aop_core.annotations.TryCatch;
import com.flyjingfish.android_aop_core.listeners.OnCustomInterceptListener;
import com.flyjingfish.android_aop_core.listeners.OnPermissionsInterceptListener;
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener;
import com.flyjingfish.android_aop_core.listeners.OnThrowableListener;
import com.flyjingfish.android_aop_core.utils.AndroidAop;
import com.flyjingfish.test_lib.PermissionRejectListener;
import com.flyjingfish.test_lib.ToastUtils;
import com.flyjingfish.test_lib.collect.InitCollect;
import com.flyjingfish.test_lib.collect.InitCollect2;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MyApp extends Application {
    public static MyApp INSTANCE;
    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.INSTANCE.setApp(this);
        INSTANCE = this;
        InitCollect.INSTANCE.init(this);
        InitCollect2.init(this);
        AndroidAop.INSTANCE.setOnPermissionsInterceptListener(new OnPermissionsInterceptListener() {
            @SuppressLint("CheckResult")
            @Override
            public void requestPermission(@NonNull ProceedJoinPoint joinPoint, @NonNull Permission permission, @NonNull OnRequestPermissionListener call) throws Throwable {
                Object target = joinPoint.getTarget();
                String[] permissions = permission.value();
                permissions = check13ReadExternalStorage(permissions);
                Log.e("requestPermission",""+permissions);
                if (target instanceof FragmentActivity){
                    RxPermissions rxPermissions = new RxPermissions((FragmentActivity) target);
                    rxPermissions.requestEach(permissions)
                            .subscribe(permissionResult -> {
                                call.onCall(permissionResult.granted);
                                if (!permissionResult.granted && target instanceof PermissionRejectListener) {
                                    ((PermissionRejectListener) target).onReject(permission,permissionResult);
                                }
                            });
                }else if (target instanceof Fragment){
                    RxPermissions rxPermissions = new RxPermissions((Fragment) target);
                    rxPermissions.requestEach(permissions)
                            .subscribe(permissionResult -> {
                                call.onCall(permissionResult.granted);
                                if (!permissionResult.granted && target instanceof PermissionRejectListener) {
                                    ((PermissionRejectListener) target).onReject(permission,permissionResult);
                                }
                            });
                }else {
                    joinPoint.proceed();
                }
            }
        });

        AndroidAop.INSTANCE.setOnCustomInterceptListener(new OnCustomInterceptListener() {
            @Nullable
            @Override
            public Object invoke(@NonNull ProceedJoinPoint joinPoint, @NonNull CustomIntercept customIntercept) throws Throwable {
                // TODO: 2023/11/11 在此写你的逻辑 在合适的地方调用 joinPoint.proceed()，
                //  joinPoint.proceed(args)可以修改方法传入的参数，如果需要改写返回值，则在 return 处返回即可
                //  不调用 proceed 就不会执行拦截切面方法内的代码
                Log.e("CustomIntercept","invoke"+(customIntercept == null));
                ToastUtils.INSTANCE.makeText(MyApp.this,"进入 @CustomIntercept 拦截器，value="+customIntercept.value()[0]);
                return joinPoint.proceed(2,(short)3,(byte)4,'5',6L,7f, 8.0d,true);
            }
        });

        AndroidAop.INSTANCE.setOnThrowableListener(new OnThrowableListener() {
            @Nullable
            @Override
            public Object handleThrowable(@NonNull String flag, @Nullable Throwable throwable,TryCatch tryCatch) {
                // TODO: 2023/11/11 发生异常可根据你当时传入的flag作出相应处理，如果需要改写返回值，则在 return 处返回即可
                Log.e("ThrowableListener","handleThrowable");
                ToastUtils.INSTANCE.makeText(MyApp.this,"@TryCatch Throwable="+throwable.getMessage());
                return 3;
            }
        });
    }

    public static String[] check13ReadExternalStorage(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (Arrays.asList(permissions).contains(android.Manifest.permission.READ_EXTERNAL_STORAGE)||Arrays.asList(permissions).contains(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            ArrayList<String> list = new ArrayList<>(permissions.length);
            Collections.addAll(list, permissions);
            list.remove(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            list.remove(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            list.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            list.add(android.Manifest.permission.READ_MEDIA_AUDIO);
            list.add(Manifest.permission.READ_MEDIA_VIDEO);
            return list.toArray(new String[0]);
        }
        return permissions;
    }
}
