package com.flyjingfish.test_lib.replace;

import android.content.Context;
import android.widget.Toast;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod;

@AndroidAopReplaceClass(
        "android.widget.Toast"
)
public class ReplaceToast {
    @AndroidAopReplaceMethod(
            "void show()"
    )
    public static void show(Toast toast) {
        toast.show();
    }
    @AndroidAopReplaceMethod(
            "android.widget.Toast makeText(android.content.Context, java.lang.CharSequence, int)"
    )
    public static Toast makeText(Context context, CharSequence text, int duration) {
        return Toast.makeText(context, "ReplaceToast-"+text, duration);
    }
}
