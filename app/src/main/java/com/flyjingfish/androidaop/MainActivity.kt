package com.flyjingfish.androidaop

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.Lifecycle
import com.flyjingfish.android_aop_core.annotations.CustomIntercept
import com.flyjingfish.android_aop_core.annotations.DoubleClick
import com.flyjingfish.android_aop_core.annotations.IOThread
import com.flyjingfish.android_aop_core.annotations.MainThread
import com.flyjingfish.android_aop_core.annotations.OnLifecycle
import com.flyjingfish.android_aop_core.annotations.Permission
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.android_aop_core.annotations.TryCatch
import com.flyjingfish.android_aop_core.enums.ThreadType
import com.flyjingfish.androidaop.databinding.ActivityMainBinding
import com.flyjingfish.androidaop.test.Round
import com.flyjingfish.androidaop.test2.StaticClass
import com.flyjingfish.androidaop.test.TestBean
import com.flyjingfish.test_lib.BaseActivity
import com.flyjingfish.test_lib.annotation.MyAnno2

class MainActivity: BaseActivity2() {

    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSingleClick.setOnClickListener {
            onSingleClick()
        }
        binding.btnDoubleClick.setOnClickListener {
            onDoubleClick()
        }
        binding.btnIOThread.setOnClickListener {
            onIOThread()
        }
        binding.btnMainThread.setOnClickListener {
            onMainThread()
        }
        binding.btnOnLifecycle.setOnClickListener {
            setLogcat("当前设置的是 OnStop 才执行，你先按Home再回来看下方日志")
            onLifecycle()
        }
        binding.btnTryCatch.setOnClickListener {
            onTryCatch()
        }
        binding.btnPermission.setOnClickListener {
            onPermission(this,3)
        }
        binding.btnCustomIntercept.setOnClickListener {
            onCustomIntercept(1,2,3,'4',5,6f, 7.0,false)
        }
        binding.btnMatchClassMethod.setOnClickListener {
            setLogcat("匹配类-方法切面 继承自的 androidx.appcompat.app.AppCompatActivity 的 startActivity 方法")
            startActivity(Intent(this,SecondActivity::class.java))
        }
        binding.btnTopFun.setOnClickListener {
            testTopFun()
        }
        binding.btnStaticMethod.setOnClickListener {
            ThirdActivity.start(this,3,object : ThirdActivity.OnPhotoSelectListener {
                override fun onBack() {
                    setLogcat("测试 Kotlin 伴生对象方法，5000毫秒内只能调用一次")
                }
            })
        }
        binding.btnStaticMethod2.setOnClickListener {
            StaticClass.onStaticPermission(this,3,object : ThirdActivity.OnPhotoSelectListener {
                override fun onBack() {
                    setLogcat("测试 Java 静态方法，5000毫秒内只能调用一次")
                }
            })
        }
        val testBean = TestBean()
        binding.btnFieldSet.setOnClickListener {
            testBean.name = "1111"
        }

        binding.btnFieldGet.setOnClickListener {
            val name = testBean.name
            onTest()
        }

        binding.btnKotlinAnno.setOnClickListener {
            onMyAnno2()
            onTest(1,"2")
        }

        binding.tvLogcat.setOnClickListener { binding.tvLogcat.text = "日志:（点此清除）\n" }
    }

    @SingleClick(5000)
    fun onSingleClick(){
        setLogcat("@SingleClick 5000毫秒内只能点击一次")
    }

    @DoubleClick(300)
    fun onDoubleClick(){
        setLogcat("@DoubleClick 300毫秒内点击两次才可进入")
    }

    @IOThread(ThreadType.SingleIO)
    fun onIOThread(){
        setLogcat("@IOThread 是否主线程="+(Looper.getMainLooper() == Looper.myLooper())+",开始睡5秒，然后进入调用主线程方法")
        Thread.sleep(5000)
        onMainThread()
    }

    @MainThread
    fun setLogcat(text:String){
        binding.tvLogcat.append(text+"\n")
    }

    @MainThread
    fun onMainThread(){
        setLogcat("@MainThread 是否主线程="+(Looper.getMainLooper() == Looper.myLooper())+"睡醒了")
    }

    @OnLifecycle(Lifecycle.Event.ON_STOP)
    fun onLifecycle(){
        setLogcat("@OnLifecycle 注解的方法进入了")
    }

    var o : Round?=null
    @TryCatch
    fun onTryCatch():Int{
        var number = 1;
        number = o!!.number
        return number
    }

    @Permission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermission(activity: BaseActivity,maxSelect :Int){
        setLogcat("@Permission 获得权限了进入了方法 activity$activity,maxSelect=$maxSelect")
    }

    @CustomIntercept("我是自定义数据")
    fun onCustomIntercept(val1 : Int,short: Short,
                          byte: Byte,char: Char,
                          long: Long,float: Float,
                          double: Double,boolean: Boolean){
        setLogcat("@CustomIntercept 进入方法=$val1,$short,$byte,$char,$long,$float,$double,$boolean")
    }

    @MyAnno2
    fun onMyAnno2(){
        setLogcat("自定义Kotlin 注解切面进入方法")
    }

}