package cash.z.ecc.android.sdk.internal

import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.UnifiedFullViewingKey
import cash.z.ecc.android.sdk.model.UnifiedSpendingKey
import cash.z.ecc.android.sdk.model.ShieldedSpendingKey
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.tool.DerivationTool

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
        seed: ByteArray,
        network: ZcashNetwork,
        account: Account
    ): UnifiedSpendingKey = derivation.deriveUnifiedSpendingKey(transparentKey, seed, network, account)


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
}
