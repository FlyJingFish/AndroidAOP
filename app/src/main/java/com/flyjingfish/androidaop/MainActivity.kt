package com.flyjingfish.androidaop

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import com.flyjingfish.android_aop_core.annotations.CheckNetwork
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
import com.flyjingfish.androidaop.test.MyOnClickListener
import com.flyjingfish.androidaop.test.MyOnClickListener2
import com.flyjingfish.androidaop.test.OrderFillTransaction
import com.flyjingfish.androidaop.test.OrderFillTransactionBean
import com.flyjingfish.androidaop.test.Round
import com.flyjingfish.androidaop.test2.StaticClass
import com.flyjingfish.androidaop.test.TestBean
import com.flyjingfish.test_lib.mycut.TestParams
import com.flyjingfish.androidaop.test.TestReplace
import com.flyjingfish.androidaop.testReplace.BaseBean
import com.flyjingfish.test_lib.BaseActivity
import com.flyjingfish.test_lib.annotation.MyAnno3
import com.flyjingfish.test_lib.PermissionRejectListener
import com.flyjingfish.test_lib.TestMatch2
import com.flyjingfish.test_lib.annotation.MyAnno2
import com.flyjingfish.test_lib.mycut.TestParams2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.Thread.sleep

class MainActivity: BaseActivity2(), PermissionRejectListener{
    //    val haha = 1
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("MainActivity","onCreate")
        val map = mapOf(1 to "num1",0 to "num")
        println(map)
        val sortedMap = map.entries.sortedBy { it.key }.associate { it.toPair() }

        println(sortedMap) // 输出: {a=1, b=2, c=3}
        println(sortedMap.values.toTypedArray()) // 输出: {a=1, b=2, c=3}
//        binding.btnSingleClick.setOnClickListener{
//            onSingleClick()
//        }
        binding.btnSingleClick.setOnClickListener(object :MyOnClickListener(){
            override fun onClick(v: View?) {
                super.onClick(v)
                try {
                    onSingleClick()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val bean = OrderFillTransactionBean(emptyList<OrderFillTransaction>(),"")
                bean.toString()
                bean.hashCode()
            }

        })

//        binding.btnDoubleClick.setOnClickListener {
//            onDoubleClick()
//        }
        binding.btnDoubleClick.setOnClickListener(object : MyOnClickListener2{
            override fun onClick(v: View?) {
                onDoubleClick()
            }

        })
        val bbean = BaseBean(0,0)
        bbean.test()
//        binding.btnIOThread.setOnClickListener {
//            onIOThread()
//        }
        binding.btnIOThread.setOnClickListener(object : MyOnClickListener2{
            override fun onClick(v: View?) {
                onIOThread()
            }
        })
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
            toSecondActivity()
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
        val round = Round()
        binding.btnFieldGet.setOnClickListener {
            val name = testBean.name
            Log.e("testBean","name=${name}")
            round.setRunnable(Runnable {  })
        }

        binding.btnKotlinAnno.setOnClickListener {
            onMyAnno2(1,2,3,4,5,6,7,8,9,10,
                11,12,13,15,15,16,17,18,19,20)
        }

        TestMatch2(1).test1()
        TestMatch2().test1()
        binding.btnTestMuch.setOnClickListener {
//
//            LocaleTransform.getLanguage(1)

            GlobalScope.launch {
//                getData2(1)
                val arg1 = getData(1)
                setLogcat("GlobalScope---arg1=$arg1")
            }
        }

        binding.tvLogcat.setOnClickListener { binding.tvLogcat.text = "日志:（点此清除）\n" }

        val bean = TestReplace()
        bean.test()

        binding.btnDemo3.setOnClickListener { toThirdActivity() }
        binding.btnDemo4.setOnClickListener { startActivity(Intent(this,FourActivity::class.java)) }
    }

    @CheckNetwork(toastText = "没有网络呀～～～")
    fun toSecondActivity(){
        startActivity(Intent(this,SecondActivity::class.java))
    }

    private fun toThirdActivity(){
        startActivity(Intent(this,ThirdActivity::class.java))
    }

    @SingleClick(5000)
    fun onSingleClick(){
//        var number = 1;
//        number = o!!.number
        throw IOException("sss")
        Log.e("MainActivity","onSingleClick")
        setLogcat("@SingleClick 5000毫秒内只能点击一次")
    }

    @DoubleClick(300)
    fun onDoubleClick(){
        setLogcat("@DoubleClick 300毫秒内点击两次才可进入")
    }

    @IOThread(ThreadType.MultipleIO)
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
    fun onPermission(activity: BaseActivity, maxSelect :Int){
        setLogcat("@Permission 获得权限了进入了方法 activity$activity,maxSelect=$maxSelect")
    }

    override fun onReject(permission:Permission,permissionResult: com.tbruyelle.rxpermissions3.Permission) {
        setLogcat("@Permission 没有获得权限，tag=${permission.tag},permissionResult=${permissionResult}")
    }

    @CustomIntercept("我是自定义数据")
    fun onCustomIntercept(val1 : Int,short: Short,
                          byte: Byte,char: Char,
                          long: Long,float: Float,
                          double: Double,boolean: Boolean){
        setLogcat("@CustomIntercept 进入方法=$val1,$short,$byte,$char,$long,$float,$double,$boolean")
    }

    @MyAnno2
    fun onMyAnno2(@TestParams("lala")@TestParams2("hehe") num1: Int,num2: Long,num3: Long,num4: Int,num5: Int,
                  num6: Int,num7: Int,num8: Int,num9: Int,num10: Int,
                  num11: Int,num12: Int,num13: Int,num14: Int,num15: Int,
                  num16: Int,num17: Int,num18: Int,num19: Int,num20: Int){
        val num100 = num1+num5
        setLogcat("自定义Kotlin 注解切面进入方法,$num100")
    }


    suspend fun getData2(num:Int):Int{
        return withContext(Dispatchers.IO) {
            getDelayResult2(num)
        }
    }

    @MyAnno3
    suspend fun getData(num:Int) :Int{
        return withContext(Dispatchers.IO) {
            getData3(num)
        }
    }

    fun getDelayResult():Int{
        sleep(5000)
        return 200
    }

    fun getDelayResult2(num:Int):Int{
        sleep(5000)
        return num
    }

    suspend fun getData3(num:Int):Int{
        Log.e("MainActivity","=====getData3=====")
        return getDelayResult()
    }

}