package com.flyjingfish.androidaop;

import android.content.Context;
import android.telephony.TelephonyManager;

public class NativeUtils {


    private static final class InstanceHolder {
        // 单例模式确保全局只有一个实例
        private static final NativeUtils instance = new NativeUtils();
    }

    public static NativeUtils getInstance() {
        return InstanceHolder.instance;
    }

    // 声明 native 方法
    public native void hello_jni();

    // 加载 C++ 编写的库
    static {
        System.loadLibrary("OBOJni"); // 库名需与 Android.mk 中的 LOCAL_MODULE 相同
    }


    public Context context;


    public TelephonyManager getTelephonyManager() {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
}
