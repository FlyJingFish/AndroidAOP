package com.flyjingfish.test_lib

import android.app.Application
import android.util.Log

open class TestApp2 :SubApplication2 {
    override fun onCreate(application: Application) {
        Log.e("TestApp2","--onCreate---")
    }
}