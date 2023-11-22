package com.flyjingfish.androidaop

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import com.flyjingfish.android_aop_core.annotations.IOThread
import com.flyjingfish.android_aop_core.annotations.MainThread
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.annotations.TryCatch
import com.flyjingfish.android_aop_core.enums.ThreadType
import com.flyjingfish.test_lib.BaseActivity

class MainActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var round :Round ?=null
        findViewById<Button>(R.id.haha).setOnClickListener {
            round = Round()
            onClick(round!!)
            startActivity(Intent(this,SecondActivity::class.java))
        }

        findViewById<Button>(R.id.haha1).setOnClickListener {
            round?.setRunnable { }

            startActivity(Intent(this,ThirdActivity::class.java))
            onSingleClick()
        }

        findViewById<Button>(R.id.haha2).setOnClickListener {
            onTest()
            round?.getRunnable()

            val number = onDoubleClick()

            Log.e("number", "====$number")
        }

    }

    override fun onResume() {
        super.onResume()
    }
//    public fun onClick(){
//        Log.e("onClick","------")
//        //插入逻辑
//        val round = Round()
//        round.setRunnable {
//            onClick1()
//        }
//
//    }

    @IOThread(ThreadType.SingleIO)
    fun onClick(round: Round){
        Log.e("Test_MainThread","是否主线程="+(Looper.getMainLooper() == Looper.myLooper()))
        Log.e("Test_MainThread","开始睡5秒")
        Thread.sleep(5000)
        Log.e("Test_MainThread","睡醒了")
        onMainThread()
    }


    @MainThread
    fun onMainThread(){
//        val testInterface = TestInterface();
//        testInterface.onTest()
        Log.e("Test_MainThread","onMainThread是否主线程="+(Looper.getMainLooper() == Looper.myLooper()))
    }

    @Permission("11111","222222")
//    @MyAnno
    fun onSingleClick(){
        Log.e("Test_click","onSingleClick")
    }

    var o :Round?=null


    @TryCatch
    fun onDoubleClick():Int{
        var number = 1;
        number = o!!.number
        return number
    }

    override fun onTest(){

    }

}