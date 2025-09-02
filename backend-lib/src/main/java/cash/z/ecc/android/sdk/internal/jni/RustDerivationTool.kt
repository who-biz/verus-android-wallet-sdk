package cash.z.ecc.android.sdk.internal.jni

import cash.z.ecc.android.sdk.internal.Derivation
import cash.z.ecc.android.sdk.internal.model.JniUnifiedSpendingKey
import cash.z.ecc.android.sdk.internal.model.JniShieldedSpendingKey
import cash.z.ecc.android.sdk.internal.model.JniSharedSecret

class RustDerivationTool private constructor() : Derivation {
    override fun deriveUnifiedFullViewingKeys(
        seed: ByteArray,
        networkId: Int,
        numberOfAccounts: Int
    ): Array<String> = deriveUnifiedFullViewingKeysFromSeed(seed, numberOfAccounts, networkId = networkId)

    override fun deriveUnifiedFullViewingKey(
        usk: JniUnifiedSpendingKey,
        networkId: Int
    ): String = deriveUnifiedFullViewingKey(usk.bytes, networkId = networkId)

    override fun deriveUnifiedSpendingKey(
        transparentKey: ByteArray,
        extendedSecretKey: ByteArray,
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): JniUnifiedSpendingKey = deriveSpendingKey(transparentKey, extendedSecretKey, seed, accountIndex, networkId = networkId)

    override fun deriveSaplingSpendingKey(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): JniShieldedSpendingKey = deriveShieldedSpendingKey(seed, accountIndex, networkId = networkId)

    override fun deriveUnifiedAddress(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): String = deriveUnifiedAddressFromSeed(seed, accountIndex = accountIndex, networkId = networkId)

    override fun deriveShieldedAddress(
        seed: ByteArray,
        networkId: Int,
        accountIndex: Int
    ): String = deriveShieldedAddressFromSeed(seed, accountIndex = accountIndex, networkId = networkId)

    override fun isValidShieldedAddress(
        address: String,
        networkId: Int,
    ): Boolean = isValidSaplingAddress(address, networkId = networkId)

    /**
     * Given a Unified Full Viewing Key string, return the associated Unified Address.
     *
     * @param viewingKey the viewing key to use for deriving the address. The viewing key is tied to
     * a specific account so no account index is required.
     *
     * @return the address that corresponds to the viewing key.
     */
    override fun deriveShieldedAddress(
        viewingKey: String,
        networkId: Int
    ): String = deriveShieldedAddressFromViewingKey(viewingKey, networkId = networkId)

    /**
     * Given a Unified Full Viewing Key string, return the associated Unified Address.
     *
     * @param viewingKey the viewing key to use for deriving the address. The viewing key is tied to
     * a specific account so no account index is required.
     *
     * @return the address that corresponds to the viewing key.
     */
    override fun deriveUnifiedAddress(
        viewingKey: String,
        networkId: Int
    ): String = deriveUnifiedAddressFromViewingKey(viewingKey, networkId = networkId)

    override fun getSymmetricKey(
        viewingKey: String, 
        ephemeralPublicKey: ByteArray,
        networkId: Int
    ): String =  getSymmetricKeyReceiver(viewingKey, ephemeralPublicKey, networkId = networkId)

    override fun generateSymmetricKey(
        saplingAddress: String,
        networkId: Int
    ): String =  generateSymmetricKeySender(saplingAddress, networkId = networkId)

    override fun getEncryptionAddress(
        seed: ByteArray,
        fromId: ByteArray,
        toId: ByteArray,
        accountIndex: Int,
        networkId: Int
    ): String =  zGetEncryptionAddress(seed, fromId, toId, accountIndex, networkId = networkId)

    companion object {
        suspend fun new(): Derivation {
            RustBackend.loadLibrary()

            return RustDerivationTool()
        }

        @JvmStatic
        private external fun deriveSpendingKey(
            transparentKey: ByteArray,
            extendedSecretKey: ByteArray,
            seed: ByteArray,
            account: Int,
            networkId: Int
        ): JniUnifiedSpendingKey

        @JvmStatic
        private external fun deriveShieldedSpendingKey(
            seed: ByteArray,
            account: Int,
            networkId: Int
        ): JniShieldedSpendingKey

        @JvmStatic
        private external fun deriveUnifiedFullViewingKeysFromSeed(
            seed: ByteArray,
            numberOfAccounts: Int,
            networkId: Int
        ): Array<String>

        @JvmStatic
        private external fun deriveUnifiedFullViewingKey(
            usk: ByteArray,
            networkId: Int
        ): String

        @JvmStatic
        private external fun deriveUnifiedAddressFromSeed(
            seed: ByteArray,
            accountIndex: Int,
            networkId: Int
        ): String

        @JvmStatic
        private external fun deriveShieldedAddressFromSeed(
            seed: ByteArray,
            accountIndex: Int,
            networkId: Int
        ): String

        @JvmStatic
        private external fun deriveUnifiedAddressFromViewingKey(
            key: String,
            networkId: Int
        ): String

        @JvmStatic
        private external fun deriveShieldedAddressFromViewingKey(
            key: String,
            networkId: Int
        ): String

        @JvmStatic
        private external fun isValidSaplingAddress(
            address: String,
            networkId: Int
        ): Boolean

        @JvmStatic
        private external fun getSymmetricKeyReceiver(
            vk: String,
            epk: ByteArray,
            networkId: Int
        ): String

        @JvmStatic
        private external fun generateSymmetricKeySender(
            saplingAddress: String,
            networkId: Int
        ): String

        @JvmStatic
        private external fun zGetEncryptionAddress(
            seed: ByteArray,
            fromId: ByteArray,
            toId: ByteArray,
            accountIndex: Int,
            networkId: Int
        ): String
    }
}
