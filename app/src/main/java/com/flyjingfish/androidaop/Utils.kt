package com.flyjingfish.androidaop

import android.widget.ImageView
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.androidaop.ReplaceImageView.ShapeType
import com.flyjingfish.test_lib.ToastUtils
import java.text.SimpleDateFormat
import java.util.Date

@SingleClick(5000)
fun testTopFun(){
    ToastUtils.makeText(MyApp.INSTANCE,"5000毫秒内只能进一次顶层函数")
}

fun getCurrentTime():String{
    val sdf = SimpleDateFormat("mm:ss")
    val date = Date()
    return sdf.format(date)
}
fun ImageView.setShapeType(shapeType: ShapeType) {
    if (this is ReplaceImageView){
        this.setShapeType(shapeType)
    }
}