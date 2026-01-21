package cash.z.ecc.android.sdk.internal.model

import androidx.annotation.Keep

/**
 * JNI class for ChannelKeys returned from Rust.
 *
 */
@Keep
class JniChannelKeys(
    val address: String,

    val extendedFullViewingKeyBytes: ByteArray,

    val internalViewingKeyBytes: ByteArray,

    val spendingKeyBytes: ByteArray? = null,
) {
    // Override to prevent leaking key material to logs
    override fun toString(): String =
        "JniChannelKeys(address=$address, extendedFullViewingKeyBytes=***, internalViewingKeyBytes=***, spendingKeyBytes=${if (spendingKeyBytes != null) "***" else "null"})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JniChannelKeys

        if (address != other.address) return false
        if (!extendedFullViewingKeyBytes.contentEquals(other.extendedFullViewingKeyBytes)) return false

        if (!internalViewingKeyBytes.contentEquals(other.internalViewingKeyBytes)) return false

        // handle nullable byte arrays safely
        if (spendingKeyBytes != null) {
            if (other.spendingKeyBytes == null) return false
            if (!spendingKeyBytes.contentEquals(other.spendingKeyBytes)) return false
        } else if (other.spendingKeyBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + extendedFullViewingKeyBytes.contentHashCode()
        result = 31 * result + internalViewingKeyBytes.contentHashCode()
        result = 31 * result + (spendingKeyBytes?.contentHashCode() ?: 0)
        return result
    }
}
