package com.flyjingfish.test_lib

import android.util.Log
import com.flyjingfish.test_lib.annotation.MyAnno3
import com.flyjingfish.test_lib.annotation.MyAnno4
import com.flyjingfish.test_lib.annotation.MyAnno5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TestSuspend {
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
    suspend fun getData2(num:Int,num2:Int) :IntArray{
        return withContext(Dispatchers.IO) {
            Log.e("MyAnnoCut","=====getData22=====2")
            intArrayOf(num,num2)
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