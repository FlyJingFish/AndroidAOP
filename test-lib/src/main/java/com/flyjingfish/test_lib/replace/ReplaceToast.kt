package com.flyjingfish.test_lib.replace

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod

@AndroidAopReplaceClass(
    "android.widget.Toast",
)
class ReplaceToast {
    @AndroidAopReplaceMethod(
        "void show()"
    )
    fun show() {
    }
}