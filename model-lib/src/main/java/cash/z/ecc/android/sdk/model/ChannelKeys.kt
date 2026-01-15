package cash.z.ecc.android.sdk.model

data class ChannelKeys(
    val address: String,
    val dfvkBytes: ByteArray,
    val ivkBytes: ByteArray?,
    val spendingKeyBytes: ByteArray?
)
