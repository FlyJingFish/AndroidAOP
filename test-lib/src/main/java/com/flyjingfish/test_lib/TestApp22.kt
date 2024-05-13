package com.flyjingfish.test_lib

import android.app.Application
import android.util.Log

class TestApp22 :TestApp2() {
    override fun onCreate(application: Application) {
        Log.e("TestApp2","--onCreate---")
    }
}