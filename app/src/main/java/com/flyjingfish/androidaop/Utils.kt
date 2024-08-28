package com.flyjingfish.androidaop

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.ImageView
import com.flyjingfish.android_aop_core.annotations.SingleClick
import com.flyjingfish.androidaop.ReplaceImageView.ShapeType
import com.flyjingfish.test_lib.ToastUtils
import java.text.SimpleDateFormat
import java.util.Date

@SingleClick(5000)
fun testTopFun() {
    ToastUtils.makeText(MyApp.INSTANCE, "5000毫秒内只能进一次顶层函数")
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("mm:ss")
    val date = Date()
    return sdf.format(date)
}

fun ImageView.setShapeType(shapeType: ShapeType) {
    if (this is ReplaceImageView) {
        this.setShapeType(shapeType)
    }
}

fun getIMEI(context: Context): String {
    var imei = ""
    try {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            imei = tm.deviceId
        } else {
            val method = TelephonyManager::class.java.getMethod("getImei");
            imei = method.invoke(tm).toString()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return imei
}