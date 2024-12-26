package com.flyjingfish.test_lib

import android.app.Application
import android.util.Log
import com.flyjingfish.test_lib.ToastUtils.makeText

class TestApp :SubApplication {
    override fun onCreate(application: Application) {
        Log.e("TestApp","--onCreate---")
        makeText("onCreate-TestApp")
    }
}