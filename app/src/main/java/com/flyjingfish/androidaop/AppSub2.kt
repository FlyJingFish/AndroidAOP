package com.flyjingfish.androidaop

import android.app.Application
import android.util.Log
import com.flyjingfish.test_lib.SubApplication2

class AppSub2:SubApplication2 {
    override fun onCreate(application: Application) {
        Log.e("AppSub2","--onCreate---")
    }
}