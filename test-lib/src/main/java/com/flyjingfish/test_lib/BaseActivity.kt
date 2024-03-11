package com.flyjingfish.test_lib

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flyjingfish.android_aop_core.annotations.SingleClick

abstract class BaseActivity :AppCompatActivity() {

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
    }

    override fun onResume() {
        super.onResume()
    }

    @SingleClick
    fun onTest(){

    }
    fun onTest(max:Int,value:String){

    }
}