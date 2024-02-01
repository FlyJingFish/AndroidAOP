package com.flyjingfish.test_lib.replace;

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
        toast.setText("文本已替换");
        toast.show();
    }
}
