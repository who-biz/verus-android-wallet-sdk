package cash.z.ecc.android.sdk.model

import cash.z.ecc.android.sdk.internal.jni.RustBackend
import cash.z.ecc.android.sdk.internal.model.JniSharedSecret

/**
 * A Shared secret for use with sapling domain kdf, for a given note or data unit.
 *
 * An instance of this class contains only a single secret used in kdf for a given VDXF
 * operation, or for a given note decryption.  It is used to derive the symmetric key.
 * As such, it is not suitable for long-term storage, export/import, or backup purposes.
 * It can always be recomputed from the message itself, with a sapling ivk.
 */
class SharedSecret private constructor(
    /**
     * The binary encoding of the Shared Secret used for sapling note and VDXF data decryption
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    private val bytes: FirstClassByteArray
) {
    internal constructor(sharedSecretJni: JniSharedSecret) : this(
        FirstClassByteArray(sharedSecretJni.bytes.copyOf())
    )

    /**
     * The binary encoding of the Shared Secret used for sapling note and VDXF data decryption
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    fun copyBytes() = bytes.byteArray.copyOf()

    // Override to prevent leaking key to logs
    override fun toString() = "SharedSecret(bytes=***)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharedSecret

        if (bytes != other.bytes) return false

        return true
    }

    override fun hashCode(): Int {
        val result = bytes.hashCode()
        return result
    }

    companion object {
        /**
         * @return an unvalidated SharedSecret
         */
        suspend fun new(
            bytes: ByteArray
        ): SharedSecret {
            val bytesCopy = bytes.copyOf()
            return SharedSecret(FirstClassByteArray(bytesCopy))
        }
    }
}
