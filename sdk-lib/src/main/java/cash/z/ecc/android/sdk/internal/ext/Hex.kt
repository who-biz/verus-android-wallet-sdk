package cash.z.ecc.android.sdk.internal.ext

/**
 * A simple utility for converting byte arrays to and from hex strings.
 */
internal object Hex {
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /**
     * Converts a byte array into its corresponding hex string representation.
     *
     * @param bytes the byte array to convert.
     * @return the hex-encoded string.
     */
    fun toHexString(bytes: ByteArray): String {
        val result = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            result[i * 2] = HEX_CHARS[v ushr 4]
            result[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(result)
    }
}