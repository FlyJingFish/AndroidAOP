package com.flyjingfish.test_lib

import android.app.Application
import android.util.Log

class TestApp :SubApplication {
    override fun onCreate(application: Application) {
        Log.e("TestApp","--onCreate---")
    }
}