package com.flyjingfish.androidaop

import android.os.Bundle
import com.flyjingfish.androidaop.databinding.ActivityThirdBinding
import com.flyjingfish.androidaop.test.Round
import com.flyjingfish.test_lib.BaseActivity

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