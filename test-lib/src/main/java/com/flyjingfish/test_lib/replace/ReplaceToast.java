package com.flyjingfish.test_lib.replace;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod;
import com.flyjingfish.android_aop_core.proxy.ProxyMethod;
import com.flyjingfish.android_aop_core.proxy.ProxyType;

@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.STATIC_METHOD)
    //  因为被替换方法是静态的，所以参数类型及顺序和被替换方法一一对应
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "ReplaceToast-"+text, duration);
    }
    @AndroidAopReplaceMethod(
            "void setGravity(int , int , int )"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.METHOD)
    //  因为被替换方法不是静态方法，所以参数第一个是被替换类，之后的参数和被替换方法一一对应
    public static void setGravity(Toast toast,int gravity, int xOffset, int yOffset) {
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
    }
    @AndroidAopReplaceMethod(
            "void show()"
    )
    @ProxyMethod(proxyClass = Toast.class,type = ProxyType.METHOD)
    //  虽然被替换方法没有参数，但以为它不是静态方法，所以第一个参数仍然是被替换类
    public static void show(Toast toast) {
        toast.show();
    }
}
