package com.flyjingfish.androidaop

import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.test_lib.ToastUtils
import com.flyjingfish.test_lib.annotation.MyAnno2

class TestBean {
    var name:String = "test"
        @SingleClick(5000)
        set(value) {
            ToastUtils.makeText(MyApp.INSTANCE,"Kotlin TestBean name.set() 5000毫秒内只能进一次")
            field = value+".set"
        }
        @SingleClick(5000)
        get() {
            ToastUtils.makeText(MyApp.INSTANCE,"Kotlin TestBean name.get() 5000毫秒内只能进一次")
            return field
        }
}