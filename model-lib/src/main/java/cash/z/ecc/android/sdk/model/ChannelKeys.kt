package cash.z.ecc.android.sdk.model

data class ChannelKeys(
    val address: String,
    val fvkBytes: ByteArray,
    val dfvkBytes: ByteArray,
    val ivkBytes: ByteArray,
    val spendingKeyBytes: ByteArray?
)
