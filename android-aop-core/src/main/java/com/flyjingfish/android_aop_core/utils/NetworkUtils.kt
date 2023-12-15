package com.flyjingfish.android_aop_core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

@SuppressLint("MissingPermission")
object NetworkUtils {

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用MOBILE流量上网
     */
    fun isMobileNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else {
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null) {
                return networkInfo.type == ConnectivityManager.TYPE_MOBILE
            }
            return false
        }
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用WIFI上网
     */
    fun isWifiNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else {
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null) {
                return networkInfo.type == ConnectivityManager.TYPE_WIFI
            }
            return false
        }
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用以太网上网
     */
    fun isEthernetNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else {
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo != null) {
                return networkInfo.type == ConnectivityManager.TYPE_ETHERNET
            }
            return false
        }
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * [NetworkCapabilities.NET_CAPABILITY_INTERNET]，表示此网络应该(maybe)能够访问internet
     *
     * 判断当前网络可以正常上网
     * 表示此连接此网络并且能成功上网。  例如，对于具有NET_CAPABILITY_INTERNET的网络，这意味着已成功检测到INTERNET连接。
     */
    fun isConnectedAvailableNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else {
            // 获取NetworkInfo对象
            val networkInfo = cm.allNetworkInfo
            if (networkInfo.isNotEmpty()) {
                for (i in networkInfo.indices) {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
            return false
        }
    }

}
