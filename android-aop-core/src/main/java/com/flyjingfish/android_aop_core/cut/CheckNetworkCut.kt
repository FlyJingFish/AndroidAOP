package com.flyjingfish.android_aop_core.cut

import android.widget.Toast
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.AndroidAopContentProvider
import com.flyjingfish.android_aop_core.annotations.CheckNetwork
import com.flyjingfish.android_aop_core.utils.AndroidAop
import com.flyjingfish.android_aop_core.utils.NetworkUtils
import com.flyjingfish.android_aop_core.utils.Utils

internal class CheckNetworkCut : BasePointCut<CheckNetwork> {
    override fun invoke(joinPoint: ProceedJoinPoint, anno: CheckNetwork): Any? {
        return when (anno.invokeListener) {
            true -> {
                when (AndroidAop.getOnCheckNetworkListener()) {
                    null -> {
                        joinPoint.proceed()
                    }
                    else -> {
                        AndroidAop.getOnCheckNetworkListener()?.invoke(
                            joinPoint,
                            anno,
                            NetworkUtils.isConnectedAvailableNetwork(AndroidAopContentProvider.appContext)
                        )
                    }
                }
            }
            false -> {
                when(NetworkUtils.isConnectedAvailableNetwork(AndroidAopContentProvider.appContext)){
                    true -> joinPoint.proceed()
                    false -> {
                        if (anno.toastText.isNotEmpty()){
                            Utils.toast(anno.toastText,Toast.LENGTH_SHORT)
                        }
                        null
                    }
                }
            }
        }
    }
}