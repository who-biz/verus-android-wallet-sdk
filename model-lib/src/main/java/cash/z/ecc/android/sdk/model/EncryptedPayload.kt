package cash.z.ecc.android.sdk.model

data class EncryptedPayload(
    val ephemeralPublicKey: String,
    val ciphertext: String,
    val symmetricKey: String?
)