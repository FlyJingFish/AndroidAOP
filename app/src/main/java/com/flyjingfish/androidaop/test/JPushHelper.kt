package com.flyjingfish.androidaop.test


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * 处理极光跳转
 */
object JPushHelper {


}


// 跳转合约交易
const val MSG_TYPE_PRO_QUOTATION_ALERT = "PRO_QUOTATION_ALERT"

// 跳转合约交易 新架构格式
const val MSG_TYPE_PRO_QUOTATION_ALERT_NEW = "PAGE_FUTURES_PRO_TRADING"

// 资产充值
const val MSG_TYPE_PAGE_ASSETS_DEPOSIT = "PAGE_ASSETS_DEPOSIT"

// 资产现货账户tab
const val MSG_TYPE_PAGE_ASSETS_SPOT_ACCOUNT = "PAGE_ASSETS_SPOT_ACCOUNT"

// 资产合约账户tab
const val MSG_TYPE_PAGE_ASSETS_FUTURES_PRO_ACCOUNT = "PAGE_ASSETS_FUTURES_PRO_ACCOUNT"

// H5
const val MSG_TYPE_PAGE_H5 = "PAGE_H5"

// 现货
const val MSG_TYPE_SPOT_TRADING = "PAGE_SPOT_TRADING"



@Serializable
@Keep
data class JPushMessage(
    @SerializedName("msg_type")
    val msgType: String?,
    @SerializedName("contract_id")
    val contractId: String?,
    @SerializedName("coin_id")
    val coinId: String?,
    @SerializedName("redirect_url")
    val redirectUrl: String?,
    @SerializedName("symbol_code")
    val symbolCode: String?,
    // 兼容老架构
    val params: JPushParams? = null
)

@Serializable
@Keep
data class JPushParams(
    @SerializedName("contract_id")
    val contractId: String?,

)
