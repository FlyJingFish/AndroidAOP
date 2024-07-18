package com.flyjingfish.androidaop

import android.os.Bundle
import android.util.Log
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.androidaop.databinding.ActivityThirdBinding
import com.flyjingfish.androidaop.test.Round
import com.flyjingfish.test_lib.annotation.MyAnno3
import com.flyjingfish.test_lib.annotation.MyAnno4
import com.flyjingfish.test_lib.annotation.MyAnno5
import com.flyjingfish.test_lib.BaseActivity
import com.flyjingfish.test_lib.TestSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FourActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnInner0.setOnClickListener {
            test0()
        }
    }
    var o : Round?=null
    private fun test0(){
        var number = 1;
        number = o!!.number
    }
}