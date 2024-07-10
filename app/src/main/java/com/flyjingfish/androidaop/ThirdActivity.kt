package com.flyjingfish.androidaop

import android.os.Bundle
import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.androidaop.databinding.ActivityThirdBinding
import com.flyjingfish.androidaop.test2.MyAnno3
import com.flyjingfish.androidaop.test2.MyAnno4
import com.flyjingfish.androidaop.test2.MyAnno5
import com.flyjingfish.androidaop.test2.MyAnnoCut3
import com.flyjingfish.androidaop.test2.MyAnnoCut4
import com.flyjingfish.androidaop.test2.MyAnnoCut5
import com.flyjingfish.test_lib.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThirdActivity : BaseActivity() {
    companion object{
        fun start(activity: MainActivity,listener:OnPhotoSelectListener?){
            start(activity,1,listener)
        }
        @SingleClick(5000)
        fun start(activity: MainActivity,maxSelect :Int,listener:OnPhotoSelectListener?){
            listener?.onBack()
        }
    }
    interface OnPhotoSelectListener {
        fun onBack()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnInner0.setOnClickListener {
            test0()
        }
        binding.btnInner.setOnClickListener {
            test()
        }
        binding.btnInner2.setOnClickListener {
            test2()
        }
    }
    fun test0(){
        GlobalScope.launch {
//                getData2(1)
            val arg1 = getData1(1)
            Log.e("MyAnnoCut","=====arg1=====$arg1")
        }
    }
    fun test(){
        GlobalScope.launch {
//                getData2(1)
            val arg1 = getData2(1)
            Log.e("MyAnnoCut","=====arg1=====$arg1")
        }
    }

    fun test2(){
        GlobalScope.launch {
//                getData2(1)
            val arg1 = getData2(1,3)
            Log.e("MyAnnoCut","=====arg1=====$arg1")
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData1(num:Int) :Int{
//        val myAnnoCut3 = MyAnnoCut3()
//        val myAnnoCut4 = MyAnnoCut4()
//        val myAnnoCut5 = MyAnnoCut5()
//        myAnnoCut3.invokeSuspend(null,null)
//        myAnnoCut4.invokeSuspend(null,null)
//        myAnnoCut5.invokeSuspend(null,null)
        return getData2(num)
    }
//    @MyAnno3
//    @MyAnno4
//    @MyAnno5
    suspend fun getData2(num:Int) :Int{
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData2=====2")
            num
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData2(num:Int,num2:Int) :Int{
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====1")
        }
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====11")
        }
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====2")
            num + num2
        }
    }
}