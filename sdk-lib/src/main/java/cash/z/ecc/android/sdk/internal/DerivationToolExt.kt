package cash.z.ecc.android.sdk.internal

import cash.z.ecc.android.sdk.internal.model.JniUnifiedSpendingKey
//import cash.z.ecc.android.sdk.internal.model.JniShieldedSpendingKey
import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.UnifiedFullViewingKey
import cash.z.ecc.android.sdk.model.UnifiedSpendingKey
import cash.z.ecc.android.sdk.model.ShieldedSpendingKey
import cash.z.ecc.android.sdk.model.ZcashNetwork

fun Derivation.deriveUnifiedAddress(
    seed: ByteArray,
    network: ZcashNetwork,
    account: Account
): String = deriveUnifiedAddress(seed, network.id, account.value)

fun Derivation.deriveUnifiedAddress(
    viewingKey: String,
    network: ZcashNetwork,
): String = deriveUnifiedAddress(viewingKey, network.id)

fun Derivation.deriveShieldedAddress(
    viewingKey: String,
    network: ZcashNetwork,
): String = deriveShieldedAddress(viewingKey, network.id)

fun Derivation.deriveShieldedAddress(
    seed: ByteArray,
    network: ZcashNetwork,
    account: Account
): String = deriveShieldedAddress(seed, network.id, account.value)

fun Derivation.deriveUnifiedSpendingKey(
    transparentKey: ByteArray,
    seed: ByteArray,
    network: ZcashNetwork,
    account: Account
): UnifiedSpendingKey = UnifiedSpendingKey(deriveUnifiedSpendingKey(transparentKey, seed, network.id, account.value))


fun Derivation.deriveSaplingSpendingKey(
    seed: ByteArray,
    network: ZcashNetwork,
    account: Account
): ShieldedSpendingKey = ShieldedSpendingKey(deriveSaplingSpendingKey(seed, network.id, account.value))

/*
fun Derivation.deriveViewingKey(
    seed: ByteArray,
    network: ZcashNetwork,
    account: Account
): ByteArray = deriveViewingKey(seed, network.id, account.value)
*/

fun Derivation.deriveUnifiedFullViewingKey(
    usk: UnifiedSpendingKey,
    network: ZcashNetwork
): UnifiedFullViewingKey =
    UnifiedFullViewingKey(
        deriveUnifiedFullViewingKey(
            JniUnifiedSpendingKey(
                usk.account.value,
                usk.copyBytes()
            ),
            network.id
        )
    )

fun Derivation.deriveUnifiedFullViewingKeysTypesafe(
    seed: ByteArray,
    network: ZcashNetwork,
    numberOfAccounts: Int
): List<UnifiedFullViewingKey> =
    deriveUnifiedFullViewingKeys(
        seed,
        network.id,
        numberOfAccounts
    ).map { UnifiedFullViewingKey(it) }
