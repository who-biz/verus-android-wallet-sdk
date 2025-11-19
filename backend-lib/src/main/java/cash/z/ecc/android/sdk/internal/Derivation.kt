package cash.z.ecc.android.sdk.internal

import cash.z.ecc.android.sdk.internal.model.JniUnifiedSpendingKey
import cash.z.ecc.android.sdk.internal.model.JniShieldedSpendingKey

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

    companion object {
        const val DEFAULT_NUMBER_OF_ACCOUNTS = 1
    }
}
