package cash.z.ecc.android.sdk.jni

import cash.z.ecc.android.sdk.rpc.LocalRpcTypes
import com.google.common.base.Strings

/**
 * Contract defining the exposed capabilities of the Rust backend.
 * This is what welds the SDK to the Rust layer.
 * It is not documented because it is not intended to be used, directly.
 * Instead, use the synchronizer or one of its subcomponents.
 */
interface RustBackendWelding {

    fun createToAddress(
        consensusBranchId: Long,
        account: Int,
        extsk: String,
        to: String,
        value: Long,
        memo: ByteArray? = byteArrayOf()
    ): Long

    fun decryptAndStoreTransaction(tx: ByteArray, chainNetwork: String)

    fun initAccountsTable(seed: ByteArray, numberOfAccounts: Int, chainNetwork: String): Array<String>

    fun initAccountsTable(vararg extfvks: String): Boolean

    fun initBlocksTable(height: Int, hash: String, time: Long, saplingTree: String): Boolean

    fun initDataDb(): Boolean

    fun isValidShieldedAddr(addr: String, chainNetwork: String): Boolean

    fun isValidTransparentAddr(addr: String, chainNetwork: String): Boolean

    fun getAddress(account: Int = 0): String

    fun getBalance(account: Int = 0): Long

    fun getBranchIdForHeight(height: Int, chainNetwork: String): Long

    fun getReceivedMemoAsUtf8(idNote: Long): String

    fun getSentMemoAsUtf8(idNote: Long): String

    fun getVerifiedBalance(account: Int = 0): Long

    fun parseTransactionDataList(tdl: LocalRpcTypes.TransactionDataList): LocalRpcTypes.TransparentTransactionList

    fun rewindToHeight(height: Int, chainNetwork: String): Boolean

    fun scanBlocks(limit: Int = -1, chainNetwork: String): Boolean

    fun validateCombinedChain(ChainNetwork: String): Int

    // Implemented by `DerivationTool`
    interface Derivation {
        fun deriveShieldedAddress(viewingKey: String): String

        fun deriveShieldedAddress(seed: ByteArray, accountIndex: Int = 0): String

        fun deriveSpendingKeys(seed: ByteArray, numberOfAccounts: Int = 1): Array<String>

        fun deriveTransparentAddress(seed: ByteArray): String

        fun deriveViewingKey(spendingKey: String): String

        fun deriveViewingKeys(seed: ByteArray, numberOfAccounts: Int = 1): Array<String>
    }
}
