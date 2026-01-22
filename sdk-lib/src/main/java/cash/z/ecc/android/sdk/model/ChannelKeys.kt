package cash.z.ecc.android.sdk.model

import cash.z.ecc.android.sdk.internal.jni.RustBackend
import cash.z.ecc.android.sdk.internal.model.JniChannelKeys

/**
 * Channel keys used by the SDK for Verus encryption channels.
 *
 * This class is an *in-memory* container for key material returned from Rust. It is not intended
 * for long-term storage, export/import, or backup purposes.
 *
 * The underlying byte encodings are unstable and MUST NOT be exposed to users.
 */
class ChannelKeys private constructor(
    val address: String,

    private val extendedFullViewingKeyBytes: FirstClassByteArray,

    private val internalViewingKeyBytes: FirstClassByteArray,

    private val spendingKeyBytes: FirstClassByteArray? = null
) {
    internal constructor(jni: JniChannelKeys) : this(
        address = jni.address,
        extendedFullViewingKeyBytes = FirstClassByteArray(jni.extendedFullViewingKeyBytes.copyOf()),
        internalViewingKeyBytes = FirstClassByteArray(jni.internalViewingKeyBytes.copyOf()),
        spendingKeyBytes = jni.spendingKeyBytes?.let { FirstClassByteArray(it.copyOf()) }
    )

    /* copy functions are for internal use only */
    fun copyExtendedFullViewingKeyBytes(): ByteArray = extendedFullViewingKeyBytes.byteArray.copyOf()

    fun copyInternalViewingKeyBytes(): ByteArray = internalViewingKeyBytes.byteArray.copyOf()

    fun copySpendingKeyBytes(): ByteArray? = spendingKeyBytes?.byteArray?.copyOf()

    override fun toString(): String = "ChannelKeys(address=$address)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChannelKeys

        if (address != other.address) return false
        if (extendedFullViewingKeyBytes != other.extendedFullViewingKeyBytes) return false
        if (internalViewingKeyBytes != other.internalViewingKeyBytes) return false
        if (spendingKeyBytes != other.spendingKeyBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + extendedFullViewingKeyBytes.hashCode()
        result = 31 * result + internalViewingKeyBytes.hashCode()
        result = 31 * result + (spendingKeyBytes?.hashCode() ?: 0)
        return result
    }

    companion object {
        suspend fun new(
            address: String,
            extendedFullViewingKeyBytes: ByteArray,
            internalViewingKeyBytes: ByteArray,
            spendingKeyBytes: ByteArray? = null
        ): Result<ChannelKeys> {
            val xfvkCopy = extendedFullViewingKeyBytes.copyOf()
            val ivkCopy = internalViewingKeyBytes.copyOf()
            val skCopy = spendingKeyBytes?.copyOf()

            RustBackend.loadLibrary()

            return runCatching {
                // TODO: Add once implemented
                // require(RustBackend.validateChannelKeys(address, xfvkCopy, ivkCopy, skCopy))

                ChannelKeys(
                    address = address,
                    extendedFullViewingKeyBytes = FirstClassByteArray(xfvkCopy),
                    internalViewingKeyBytes = FirstClassByteArray(ivkCopy),
                    spendingKeyBytes = skCopy?.let { FirstClassByteArray(it) }
                )
            }
        }
    }
}
