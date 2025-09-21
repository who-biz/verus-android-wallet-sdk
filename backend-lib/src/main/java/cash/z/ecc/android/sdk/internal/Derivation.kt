package cash.z.ecc.android.sdk.internal

import cash.z.ecc.android.sdk.internal.model.JniUnifiedSpendingKey
import cash.z.ecc.android.sdk.internal.model.JniShieldedSpendingKey
import cash.z.ecc.android.sdk.internal.model.JniSharedSecret
import cash.z.ecc.android.sdk.model.ChannelKeys
import cash.z.ecc.android.sdk.model.EncryptedPayload

interface Derivation {
    fun deriveUnifiedAddress(
        viewingKey: String,
        networkId: Int
    ): String

    fun deriveShieldedAddress(
        viewingKey: String,
        networkId: Int
    ): String

    fun deriveUnifiedAddress(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): String

    fun deriveShieldedAddress(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): String

    fun deriveUnifiedSpendingKey(
        transparentKey: ByteArray,
        extendedSecretKey: ByteArray,
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): JniUnifiedSpendingKey


    fun deriveSaplingSpendingKey(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): JniShieldedSpendingKey

    /**
     * @return a unified full viewing key.
     */
    fun deriveUnifiedFullViewingKey(
        usk: JniUnifiedSpendingKey,
        networkId: Int
    ): String

    /**
     * @param numberOfAccounts Use [DEFAULT_NUMBER_OF_ACCOUNTS] to derive a single key.
     * @return an array of unified full viewing keys, one for each account.
     */
    fun deriveUnifiedFullViewingKeys(
        seed: ByteArray,
        networkId: Int,
        numberOfAccounts: Int
    ): Array<String>

    fun isValidShieldedAddress(
        address: String,
        networkId: Int
    ): Boolean

    fun getSymmetricKey(
        viewingKey: String,
        ephemeralPublicKey: ByteArray,
        networkId: Int
    ): String

    fun generateSymmetricKey(
        saplingAddress: String,
        networkId: Int
    ): String

    fun getEncryptionAddress(
        seed: ByteArray,
        fromId: ByteArray,
        toId: ByteArray,
        accountIndex: Int,
        networkId: Int
    ): String

    fun getVerusEncryptionAddress(
        seed: String?,
        spendingKey: String?,
        hdIndex: Int,
        encryptionIndex: Int,
        fromId: String?,
        toId: String?,
        returnSecret: Boolean
    ): ChannelKeys

    fun encryptVerusMessage(
        addressString: String,
        message: String,
        returnSsk: Boolean
    ): EncryptedPayload

    fun decryptVerusMessage(
        fvkHex: String?,
        ephemeralPublicKeyHex: String?,
        ciphertextHex: String,
        symmetricKeyHex: String?
    ): String

    companion object {
        const val DEFAULT_NUMBER_OF_ACCOUNTS = 1
    }
}
