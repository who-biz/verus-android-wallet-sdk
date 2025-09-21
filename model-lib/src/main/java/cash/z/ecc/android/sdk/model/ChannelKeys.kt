package cash.z.ecc.android.sdk.model

data class ChannelKeys (
    val address: String,
    val fullViewingKey: String,
    val spendingKey: String?
)