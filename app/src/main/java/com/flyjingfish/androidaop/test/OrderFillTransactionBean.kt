package com.flyjingfish.androidaop.test;

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 历史委托详情bean
 */
@Serializable
data class OrderFillTransactionBean(
    val dataList: List<OrderFillTransaction>,
    val nextPageOffsetData: String
)

@Serializable
data class OrderFillTransaction(
    val id: String,

    @SerialName("accountId")
    val contractId: String,

    @SerializedName("coinId")
    val coinID: String,

    @SerialName("contractId")
    val contractID: String,

    @SerialName("orderId")
    val orderID: String,

    val marginMode: String,
    val separatedMode: String,

    @SerialName("separatedOpenOrderId")
    val separatedOpenOrderID: String,

    val positionSide: String,
    val orderSide: String,
    val fillSize: String,
    val fillValue: String,
    val fillFee: String,
    val liquidateFee: String,
    val realizePnl: String,
    val direction: String,
    val liquidateType: String,
    val legacyOrderDirection: String,
    val version: String,

    @SerialName("closePositionId")
    val closePositionID: String,

    @SerialName("closePositionTransactionId")
    val closePositionTransactionID: String,

    @SerialName("closeCollateralTransactionId")
    val closeCollateralTransactionID: String,

    @SerialName("closeMarginMoveInCollateralTransactionId")
    val closeMarginMoveInCollateralTransactionID: String,

    @SerialName("openPositionId")
    val openPositionID: String,

    @SerialName("openPositionTransactionId")
    val openPositionTransactionID: String,

    @SerialName("openMarginMoveOutCollateralTransactionId")
    val openMarginMoveOutCollateralTransactionID: String,

    @SerialName("openCollateralTransactionId")
    val openCollateralTransactionID: String,

    @SerialName("matchSequenceId")
    val matchSequenceID: String,

    val matchIndex: String,
    val matchTime: String,

    @SerialName("matchAccountId")
    val matchAccountID: String,

    @SerialName("matchOrderId")
    val matchOrderID: String,

    @SerialName("matchFillId")
    val matchFillID: String,

    val extraType: String,

    @SerialName("extraDataJson")
    val extraDataJSON: String,

    val createdTime: String,
    val updatedTime: String,
    val fallback: Boolean,
    val liquidate: Boolean,
    val deleverage: Boolean,
    val withoutMatch: Boolean
)

