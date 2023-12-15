package com.flyjingfish.android_aop_core.annotations

import android.widget.Toast
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.CheckNetworkCut
import com.flyjingfish.android_aop_core.listeners.OnCheckNetworkListener
import com.flyjingfish.android_aop_core.utils.AndroidAop

/**
 * 检查网络是否链接
 */
@AndroidAopPointCut(CheckNetworkCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class CheckNetwork(
    /**
     * 设置一个标记，例如在哪个地方检查的网络
     */
    val tag: String = "",
    /**
     * 是否将结果从监听器[OnCheckNetworkListener]返回，默认false 没有网络则不继续执行切点方法逻辑，返回 true 则逻辑交由 [OnCheckNetworkListener] 处理
     */
    val invokeListener: Boolean = false,
    /**
     * 如果 [invokeListener] 返回 false，并且 [toastText] 不为空，那么没有网时就 [Toast] 提示，不满意库里写的 [Toast] 可以通过 [AndroidAop.setOnToastListener] 替换
     */
    val toastText: String = "")