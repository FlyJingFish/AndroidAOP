package  com.flyjingfish.androidaop.test

import kotlinx.serialization.*

@Serializable
data class CollateralTransactionPage (
    val dataList: MutableList<CollateralTransactionPageBean>,
    val nextFlag: Boolean,
    val totals: Long,
    val nextKey: NextKey,
)

@Serializable
data class NextKey(
    val accountId: String,
    val createdTime: String,
    val id: String
)

@Serializable
data class CollateralTransactionPageBean (
    val id: String,

    @SerialName("accountId")
    val accountID: String,

    @SerialName("coinId")
    val coinID: String,

    val marginMode: String,

    @SerialName("crossContractId")
    val crossContractID: String,

    @SerialName("isolatedPositionId")
    val isolatedPositionID: String,

    val type: String,
    val typeDesc: String,
    val deltaAmount: String,
    val deltaPendingDepositAmount: String,
    val deltaPendingWithdrawAmount: String,
    val deltaPendingTransferInAmount: String,
    val deltaPendingTransferOutAmount: String,
    val deltaIsLiquidating: Boolean,
    val deltaLegacyAmount: String,
    val afterAmount: String,
    val afterPendingDepositAmount: String,
    val afterPendingWithdrawAmount: String,
    val afterPendingTransferInAmount: String,
    val afterPendingTransferOutAmount: String,
    val afterIsLiquidating: Boolean,
    val afterLegacyAmount: String,
    val fillSize: String,
    val fillValue: String,
    val fillFee: String,
    val liquidateFee: String,
    val realizePnl: String,
    val deltaIsolatedMargin: String,
    val version: String,
    val remark : String,

    @SerialName("depositId")
    val depositID: String,

    @SerialName("withdrawId")
    val withdrawID: String,

    @SerialName("transferInId")
    val transferInID: String,

    @SerialName("transferOutId")
    val transferOutID: String,

    @SerialName("orderId")
    val orderID: String,

    @SerialName("orderFillTransactionId")
    val orderFillTransactionID: String,

    @SerialName("orderFillAccountId")
    val orderFillAccountID: String,

    @SerialName("positionId")
    val positionID: String,

    @SerialName("positionTransactionId")
    val positionTransactionID: String,

    @SerialName("relatedCollateralTransactionId")
    val relatedCollateralTransactionID: String,

    val marginMoveReason: String,

    @SerialName("contractId")
    val contractId: String,

    val positionSide: String,
    val transferReason: String,
    val liquidateType: String,
    val legacyOrderDirection: String,
    val extraType: String,

    @SerialName("extraDataJson")
    val extraDataJSON: String,

    val createdTime: String,
    val updatedTime: String
): CommonBaseBean()