package cash.z.ecc.android.sdk.model

data class ChannelKeys(
    val address: String,
    val fvk: String,
    val fvkHex: String,
    val dfvkHex: String,
    val ivk: String?,
    val spendingKey: String?
)