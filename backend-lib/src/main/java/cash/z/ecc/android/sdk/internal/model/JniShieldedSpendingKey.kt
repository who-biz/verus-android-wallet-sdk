package cash.z.ecc.android.sdk.internal.model

import androidx.annotation.Keep

/**
 * A Shielded Spending Key. For Verus, this is exclusively sapling
 *
 * This is the spend authority for an account under the wallet's seed.
 *
 * An instance of this class contains only the sapling spending key for a wallet
 */
@Keep
class JniShieldedSpendingKey(
    val account: Int,
    /**
     * The binary encoding of the Sapling spending key for [account].
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    val bytes: ByteArray
) {
    // Override to prevent leaking key to logs
    override fun toString() = "JniShieldedSpendingKey(account=$account, bytes=***)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JniShieldedSpendingKey

        if (account != other.account) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + bytes.hashCode()
        return result
    }
}
