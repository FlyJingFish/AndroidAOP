package com.flyjingfish.androidaop

import android.os.Bundle
import android.util.Log
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.androidaop.databinding.ActivityThirdBinding
import com.flyjingfish.test_lib.annotation.MyAnno3
import com.flyjingfish.test_lib.annotation.MyAnno4
import com.flyjingfish.test_lib.annotation.MyAnno5
import com.flyjingfish.test_lib.BaseActivity
import com.flyjingfish.test_lib.TestSuspend
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
        binding.btnInner1.setOnClickListener {
            test1()
        }
        binding.btnInner2.setOnClickListener {
            test2()
        }
        binding.btnInner3.setOnClickListener {
            test3()
        }

        binding.btnInner4.setOnClickListener {
            test4()
        }
        binding.btnInner5.setOnClickListener {
            test5()
        }
        binding.btnInner6.setOnClickListener {
            test6()
        }
        binding.btnInner7.setOnClickListener {
            test7()
        }
    }
    fun test0(){
        //间接调用 suspend 中包含切换一个线程的 函数
        GlobalScope.launch {
            val arg1 = getData1(1)
            Log.e("MyAnnoCut","=====test0=====$arg1")
        }
    }
    fun test1(){
        //直接调用 suspend 中包含切换线程的 函数
        GlobalScope.launch {
            val arg1 = getData2(1,3)
            Log.e("MyAnnoCut","=====test1=====$arg1")
        }
    }

    fun test2(){
        //直接调用 suspend 中包含多个切换线程的 函数
        GlobalScope.launch {
            val arg1 = getData3(1,3)
            Log.e("MyAnnoCut","=====test2=====$arg1")
        }
    }

    fun test3(){
        //间接调用 suspend 中包含切换多个线程的 函数
        GlobalScope.launch {
            val arg1 = getData11(1)
            Log.e("MyAnnoCut","=====test3=====$arg1")
        }
    }


    fun test4(){
        //间接调用 suspend 中包含切换一个线程的 函数
        GlobalScope.launch {
            val arg1 = TestSuspend.getData1(1)
            Log.e("MyAnnoCut","=====test4=====$arg1")
        }
    }
    fun test5(){
        //直接调用 suspend 中包含切换线程的 函数
        GlobalScope.launch {
            val arg1 = TestSuspend.getData2(1,3)
            Log.e("MyAnnoCut","=====test5=====$arg1")
        }
    }

    fun test6(){
        //直接调用 suspend 中包含多个切换线程的 函数
        GlobalScope.launch {
            val arg1 = TestSuspend.getData3(1,3)
            Log.e("MyAnnoCut","=====test6=====$arg1")
        }
    }

    fun test7(){
        //间接调用 suspend 中包含切换多个线程的 函数
        GlobalScope.launch {
            val arg1 = TestSuspend.getData11(1)
            Log.e("MyAnnoCut","=====test7=====$arg1")
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData1(num:Int) :Int{
        return getData2(num)
    }

    suspend fun getData2(num:Int) :Int{
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData2=====2")
            num
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData11(num:Int) :Int{
        return getData22(num)
    }

    suspend fun getData22(num:Int) :Int{
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====1")
        }
        withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====11")
        }
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData2=====2")
            num
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData2(num:Int,num2:Int) :Int{
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====2")
            num + num2
        }
    }

    @MyAnno3
    @MyAnno4
    @MyAnno5
    suspend fun getData3(num:Int,num2:Int) :Int{
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