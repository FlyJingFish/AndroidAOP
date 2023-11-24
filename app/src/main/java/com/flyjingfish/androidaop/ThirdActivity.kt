package com.flyjingfish.androidaop

import android.os.Bundle
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.test_lib.BaseActivity

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
    }
}