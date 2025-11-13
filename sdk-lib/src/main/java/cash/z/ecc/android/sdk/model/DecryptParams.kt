package cash.z.ecc.android.sdk.model


data class DecryptParams(
    val dfvkHex: String?,
    val ephemeralPublicKeyHex: String?,
    val ciphertextHex: String,
    val symmetricKeyHex: String?
)