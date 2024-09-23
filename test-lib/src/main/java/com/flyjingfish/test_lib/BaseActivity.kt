package com.flyjingfish.test_lib

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.test_lib.mycut.TestParams
import com.flyjingfish.test_lib.mycut.TestParams2

abstract class BaseActivity :AppCompatActivity() {

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
    }

    open fun baseMultParam(@TestParams("lala")@TestParams2("hehe") num1: Int, num2: Long, num3: Long, num4: Int, num5: Int,
                  num6: Int, num7: Int, num8: Int, num9: Int, num10: Int,
                  num11: Int, num12: Int, num13: Int, num14: Int, num15: Int,
                  num16: Int, num17: Int, num18: Int, num19: Int, num20: Int):Int{
        val num100 = num1+num5
        return num100
    }
    @SingleClick
    fun onTest(){

    }
    fun onTest(maxs:Int,value:String){

    }
}