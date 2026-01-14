package cash.z.ecc.android.sdk.internal

import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.EphemeralPublicKey
import cash.z.ecc.android.sdk.model.UnifiedFullViewingKey
import cash.z.ecc.android.sdk.model.UnifiedSpendingKey
import cash.z.ecc.android.sdk.model.ShieldedSpendingKey
import cash.z.ecc.android.sdk.model.SharedSecret
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.tool.DerivationTool
import cash.z.ecc.android.sdk.internal.ext.Hex
import cash.z.ecc.android.sdk.model.ChannelKeys
import cash.z.ecc.android.sdk.model.EncryptedPayload

internal class TypesafeDerivationToolImpl(private val derivation: Derivation) : DerivationTool {
    override suspend fun deriveUnifiedFullViewingKeys(
        seed: ByteArray,
        network: ZcashNetwork,
        numberOfAccounts: Int
    ): List<UnifiedFullViewingKey> = derivation.deriveUnifiedFullViewingKeysTypesafe(seed, network, numberOfAccounts)

    override suspend fun deriveUnifiedFullViewingKey(
        usk: UnifiedSpendingKey,
        network: ZcashNetwork
    ): UnifiedFullViewingKey = derivation.deriveUnifiedFullViewingKey(usk, network)


/*    override suspend fun deriveViewingKey(
        seed: ByteArray,
        network: ZcashNetwork
    ): ByteArray = derivation.deriveViewingKey(seed, network)
*/
    override suspend fun deriveUnifiedSpendingKey(
        transparentKey: ByteArray,
        extendedSecretKey: ByteArray,
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): UnifiedSpendingKey = derivation.deriveUnifiedSpendingKey(transparentKey, extendedSecretKey, seed, network, account)


    override suspend fun deriveSaplingSpendingKey(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): ShieldedSpendingKey = derivation.deriveSaplingSpendingKey(seed, network, account)


    override suspend fun deriveUnifiedAddress(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): String = derivation.deriveUnifiedAddress(seed, network, account)

    override suspend fun deriveUnifiedAddress(
        viewingKey: String,
        network: ZcashNetwork,
    ): String = derivation.deriveUnifiedAddress(viewingKey, network)

    override suspend fun deriveShieldedAddress(
        viewingKey: String,
        network: ZcashNetwork,
    ): String = derivation.deriveShieldedAddress(viewingKey, network)

    override suspend fun deriveShieldedAddress(
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): String = derivation.deriveShieldedAddress(seed, network, account)

    override suspend fun isValidShieldedAddress(
        address: String,
        network: ZcashNetwork
    ): Boolean = derivation.isValidShieldedAddress(address, network)

    override suspend fun getSymmetricKey(
        viewingKey: String,
        ephemeralPublicKey: ByteArray,
        network: ZcashNetwork
    ): String = derivation.getSymmetricKey(viewingKey, ephemeralPublicKey, network)

    override suspend fun generateSymmetricKey(
        saplingAddress: String,
        network: ZcashNetwork
    ): String = derivation.generateSymmetricKey(saplingAddress, network)

    override suspend fun getEncryptionAddress(
        seed: ByteArray,
        fromId: ByteArray,
        toId: ByteArray,
        accountIndex: Int,
        network: ZcashNetwork
    ): String = derivation.getEncryptionAddress(seed, fromId, toId, accountIndex, network)

    override suspend fun getVerusEncryptionAddress(
        seed: ByteArray?,
        spendingKey: ByteArray?,
        fromId: String?,
        toId: String?,
        hdIndex: Int,
        encryptionIndex: Int,
        returnSecret: Boolean
    ): ChannelKeys {
        // We must call the NEW function "getVerusEncryptionAddress" on the derivation object,
        // not the old function "getEncryptionAddress".
        return derivation.getVerusEncryptionAddress(
            seed = seed,
            spendingKey = spendingKey,
            hdIndex = hdIndex,
            encryptionIndex = encryptionIndex,
            fromId = fromId,
            toId = toId,
            returnSecret = returnSecret
        )
    }

    override suspend fun encryptVerusMessage(
        address: String,
        message: String,
        returnSsk: Boolean
    ): EncryptedPayload {
        // These parameters don't need conversion, so we just pass them through.
        return derivation.encryptVerusMessage(address, message, returnSsk)
    }

    override suspend fun decryptVerusMessage(
        fvkHex: String?,
        epkHex: String?,
        ciphertextHex: String,
        sskHex: String?
    ): String {
        // These parameters also don't need conversion, so we just pass them through.
        return derivation.decryptVerusMessage(fvkHex, epkHex, ciphertextHex, sskHex)
    }
}
