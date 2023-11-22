package com.flyjingfish.test_lib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    open fun onTest(){

    }
}