package com.flyjingfish.androidaop

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.google.android.material.textview.MaterialTextView

@AndroidAopReplaceClass(value = "android.widget.TextView", type = MatchType.EXTENDS)
object HookTextViewInit {

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet,int,int)")
    @JvmStatic
    fun getTextView1(textview: TextView): TextView {
        android.util.Log.e(
            "ResourcesPack",
            "HookTextViewInit: Initializing TextView with HookTextView getTextView1"
        )
        return textview
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet,int)")
    @JvmStatic
    fun getTextView2(textview: TextView): TextView {
        android.util.Log.e(
            "ResourcesPack",
            "HookTextViewInit: Initializing TextView with HookTextView getTextView2"
        )
        return textview
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context,android.util.AttributeSet)")
    @JvmStatic
    fun getTextView3(clazz: Class<*>,context: Context,set: AttributeSet): TextView {
        android.util.Log.e(
            "ResourcesPack",
            "HookTextViewInit: Initializing TextView with HookTextView getTextView3=$clazz"
        )

        return clazz.getConstructor(Context::class.java,AttributeSet::class.java).newInstance(context,set) as TextView
    }

    @AndroidAopReplaceMethod("<init>(android.content.Context)")
    @JvmStatic
    fun getTextView4(textview: TextView): TextView {
        android.util.Log.e(
            "ResourcesPack",
            "HookTextViewInit: Initializing TextView with HookTextView getTextView4 #${textview}#${textview.context}#${textview.text}"
        )
        textview.text = "Initialized TextView"
        return textview
    }
}