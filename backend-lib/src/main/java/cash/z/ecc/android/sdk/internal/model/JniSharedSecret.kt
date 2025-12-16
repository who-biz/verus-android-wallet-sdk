package cash.z.ecc.android.sdk.internal.model

import androidx.annotation.Keep

/**
 * A Shared DH secret, for construction of symmetric key in note/data decryption.
 * For Verus, this is exclusively sapling
 *
 * This is used on ciphertext decryption operations for both received notes, and 
 * decryption of received VDXF data 
 * 
 *
 * An instance of this class contains only the shared secret for a given symmetric key
 */
@Keep
class JniSharedSecret(
    /**
     * The binary encoding of the the Shared Secret for note-style decryption (DH secret).
     *
     * This encoding **MUST NOT** be exposed to users. It is an internal encoding that is
     * inherently unstable, and only intended to be passed between the SDK and the storage
     * backend. Wallets **MUST NOT** allow this encoding to be exported or imported.
     */
    val bytes: ByteArray
) {
    // Override to prevent leaking key to logs
    override fun toString() = "JniSharedSecret(bytes=***)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JniSharedSecret

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.hashCode()
        return result
    }
}
