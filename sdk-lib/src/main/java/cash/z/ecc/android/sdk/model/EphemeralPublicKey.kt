package cash.z.ecc.android.sdk.model

import cash.z.ecc.android.sdk.internal.jni.RustBackend

/**
 * An ephemeral public key for use with sapling domain kdf, for a given note or data unit.
 *
 * An instance of this class contains only a single ephemeral pk used in kdf for a given VDXF
 * operation, or for a given note decryption.  It is used to derive the symmetric key.
 * As such, it is not suitable for long-term storage, export/import, or backup purposes.
 * It can always be recomputed from the message itself, with a sapling ivk.
 */
class EphemeralPublicKey private constructor(
    /**
     * The binary encoding of the Ephemeral Public Key used for sapling note and VDXF data decryption
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    private val bytes: FirstClassByteArray
) {
    internal constructor(EphemeralKeyBytes: ByteArray) : this(
        FirstClassByteArray(EphemeralKeyBytes.copyOf())
    )

    /**
     * The binary encoding of the Ephemeral Public Key used for sapling note and VDXF data decryption
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    fun copyBytes() = bytes.byteArray.copyOf()

    // Override to prevent leaking key to logs
    override fun toString() = "EphemeralPublicKey(bytes=***)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EphemeralPublicKey

        if (bytes != other.bytes) return false

        return true
    }

    override fun hashCode(): Int {
        val result = bytes.hashCode()
        return result
    }

    companion object {
        /**
         * @return an unvalidated EphemeralPublicKey
         */
        suspend fun new(
            bytes: ByteArray
        ): EphemeralPublicKey {
            val bytesCopy = bytes.copyOf()
            return EphemeralPublicKey(FirstClassByteArray(bytesCopy))
        }
    }
}
