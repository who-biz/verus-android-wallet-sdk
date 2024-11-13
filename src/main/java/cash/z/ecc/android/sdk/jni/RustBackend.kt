package cash.z.ecc.android.sdk.jni

import cash.z.ecc.android.sdk.exception.BirthdayException
import cash.z.ecc.android.sdk.ext.ZcashSdk.OUTPUT_PARAM_FILE_NAME
import cash.z.ecc.android.sdk.ext.ZcashSdk.SPEND_PARAM_FILE_NAME
import cash.z.ecc.android.sdk.ext.twig
import cash.z.ecc.android.sdk.rpc.LocalRpcTypes
import java.io.File

/**
 * Serves as the JNI boundary between the Kotlin and Rust layers. Functions in this class should
 * not be called directly by code outside of the SDK. Instead, one of the higher-level components
 * should be used such as Wallet.kt or CompactBlockProcessor.kt.
 */
class RustBackend private constructor() : RustBackendWelding {

    fun getChainNetworkId(chainNetwork: String): UShort {
        var networkId: Int
        when (chainNetwork) {
            "VRSC" -> networkId = 1
            "ZEC" -> networkId = 2
            else -> networkId = 0
        }
        return networkId as UShort
    }

    init {
        load()
    }

    // Paths
    lateinit var pathDataDb: String
    internal set
    lateinit var pathCacheDb: String
    internal set
    lateinit var pathParamsDir: String
    internal set
    lateinit var chainNetwork: String
    internal set

    internal var birthdayHeight: Int = -1
        get() = if (field != -1) field else throw BirthdayException.UninitializedBirthdayException
        private set

    fun clear(clearCacheDb: Boolean = true, clearDataDb: Boolean = true) {
        if (clearCacheDb) {
            twig("Deleting the cache database!")
            File(pathCacheDb).delete()
        }
        if (clearDataDb) {
            twig("Deleting the data database!")
            File(pathDataDb).delete()
        }
    }


    //
    // Wrapper Functions
    //

    override fun initDataDb() = initDataDb(pathDataDb)

    override fun initAccountsTable(vararg extfvks: String) =
        initAccountsTableWithKeys(pathDataDb, extfvks, getChainNetworkId(chainNetwork))

    override fun initAccountsTable(
        seed: ByteArray,
        numberOfAccounts: Int,
        chainNetwork: String
    ) = initAccountsTable(pathDataDb, seed, numberOfAccounts, getChainNetworkId(chainNetwork))

    override fun initBlocksTable(
        height: Int,
        hash: String,
        time: Long,
        saplingTree: String
    ): Boolean {
        return initBlocksTable(pathDataDb, height, hash, time, saplingTree)
    }

    override fun getAddress(account: Int) = getAddress(pathDataDb, account)

    override fun getBalance(account: Int) = getBalance(pathDataDb, account)

    override fun getVerifiedBalance(account: Int) = getVerifiedBalance(pathDataDb, account)

    override fun getReceivedMemoAsUtf8(idNote: Long) =
        getReceivedMemoAsUtf8(pathDataDb, idNote)

    override fun getSentMemoAsUtf8(idNote: Long) = getSentMemoAsUtf8(pathDataDb, idNote)

    override fun validateCombinedChain(chainNetwork: String) = validateCombinedChain(pathCacheDb, pathDataDb, getChainNetworkId(chainNetwork))

    override fun rewindToHeight(height: Int, chainNetwork: String) = rewindToHeight(pathDataDb, height, getChainNetworkId(chainNetwork))

    override fun scanBlocks(limit: Int, chainNetwork: String): Boolean {
        return if (limit > 0) {
            scanBlockBatch(pathCacheDb, pathDataDb, limit, getChainNetworkId(chainNetwork))
        } else {
            scanBlocks(pathCacheDb, pathDataDb, getChainNetworkId(chainNetwork))
        }
    }

    override fun decryptAndStoreTransaction(tx: ByteArray, chainNetwork: String) = decryptAndStoreTransaction(pathDataDb, tx, getChainNetworkId(chainNetwork))

    override fun createToAddress(
        consensusBranchId: Long,
        account: Int,
        extsk: String,
        to: String,
        value: Long,
        memo: ByteArray?,
    ): Long = createToAddress(
        pathDataDb,
        consensusBranchId,
        account,
        extsk,
        to,
        value,
        memo ?: ByteArray(0),
        "${pathParamsDir}/$SPEND_PARAM_FILE_NAME",
        "${pathParamsDir}/$OUTPUT_PARAM_FILE_NAME",
        getChainNetworkId(chainNetwork)
    )

    override fun isValidShieldedAddr(addr: String, chainNetwork: String) = isValidShieldedAddress(addr, getChainNetworkId(chainNetwork))

    override fun isValidTransparentAddr(addr: String, chainNetwork: String) = isValidTransparentAddress(addr, getChainNetworkId(chainNetwork))

    override fun getBranchIdForHeight(height: Int, chainNetwork: String): Long = branchIdForHeight(height, getChainNetworkId(chainNetwork))

    /**
     * This is a proof-of-concept for doing Local RPC, where we are effectively using the JNI
     * boundary as a grpc server. It is slightly inefficient in terms of both space and time but
     * given that it is all done locally, on the heap, it seems to be a worthwhile tradeoff because
     * it reduces the complexity and expands the capacity for the two layers to communicate.
     *
     * We're able to keep the "unsafe" byteArray functions private and wrap them in typeSafe
     * equivalents and, eventually, surface any parse errors (for now, errors are only logged).
     */
    override fun parseTransactionDataList(tdl: LocalRpcTypes.TransactionDataList): LocalRpcTypes.TransparentTransactionList {
        return try {
            // serialize the list, send it over to rust and get back a serialized set of results that we parse out and return
            return LocalRpcTypes.TransparentTransactionList.parseFrom(parseTransactionDataList(tdl.toByteArray()))
        } catch (t: Throwable) {
            twig("ERROR: failed to parse transaction data list due to: $t caused by: ${t.cause}")
            LocalRpcTypes.TransparentTransactionList.newBuilder().build()
        }
    }

    /**
     * Exposes all of the librustzcash functions along with helpers for loading the static library.
     */
    companion object {
        private var loaded = false

        /**
         * Loads the library and initializes path variables. Although it is best to only call this
         * function once, it is idempotent.
         */
        fun init(
            cacheDbPath: String,
            dataDbPath: String,
            paramsPath: String,
            birthdayHeight: Int? = null,
            chainNetwork: String
        ): RustBackend {
            return RustBackend().apply {
                pathCacheDb = cacheDbPath
                pathDataDb = dataDbPath
                pathParamsDir = paramsPath
                if (birthdayHeight != null) {
                    this.birthdayHeight = birthdayHeight
                }
                this.chainNetwork = chainNetwork

            }
        }

        fun load() {
            // It is safe to call these things twice but not efficient. So we add a loose check and
            // ignore the fact that it's not thread-safe.
            if (!loaded) {
                twig("Loading RustBackend") {
                    loadRustLibrary()
                    initLogs()
                }
            }
        }

        /**
         * The first call made to this object in order to load the Rust backend library. All other
         * external function calls will fail if the libraries have not been loaded.
         */
        private fun loadRustLibrary() {
            try {
                System.loadLibrary("zcashwalletsdk")
                loaded = true
            } catch (e: Throwable) {
                twig("Error while loading native library: ${e.message}")
            }
        }


        //
        // External Functions
        //

        @JvmStatic private external fun initDataDb(dbDataPath: String): Boolean

        @JvmStatic private external fun initAccountsTable(
            dbDataPath: String,
            seed: ByteArray,
            accounts: Int,
            chainNetwork: UShort
        ): Array<String>

        @JvmStatic private external fun initAccountsTableWithKeys(
            dbDataPath: String,
            extfvk: Array<out String>,
            chainNetwork: UShort
        ): Boolean

        @JvmStatic private external fun initBlocksTable(
            dbDataPath: String,
            height: Int,
            hash: String,
            time: Long,
            saplingTree: String
        ): Boolean

        @JvmStatic private external fun getAddress(dbDataPath: String, account: Int): String

        @JvmStatic private external fun isValidShieldedAddress(addr: String, chainNetwork: UShort): Boolean

        @JvmStatic private external fun isValidTransparentAddress(addr: String, chainNetwork: UShort): Boolean

        @JvmStatic private external fun getBalance(dbDataPath: String, account: Int): Long

        @JvmStatic private external fun getVerifiedBalance(dbDataPath: String, account: Int): Long

        @JvmStatic private external fun getReceivedMemoAsUtf8(dbDataPath: String, idNote: Long): String

        @JvmStatic private external fun getSentMemoAsUtf8(dbDataPath: String, idNote: Long): String

        @JvmStatic private external fun validateCombinedChain(dbCachePath: String, dbDataPath: String, chainNetwork: UShort): Int

        @JvmStatic private external fun rewindToHeight(dbDataPath: String, height: Int, chainNetwork: UShort): Boolean

        @JvmStatic private external fun scanBlocks(dbCachePath: String, dbDataPath: String, chainNetwork: UShort): Boolean

        @JvmStatic private external fun scanBlockBatch(dbCachePath: String, dbDataPath: String, limit: Int, chainNetwork: UShort): Boolean

        @JvmStatic private external fun decryptAndStoreTransaction(dbDataPath: String, tx: ByteArray, chainNetwork: UShort)

        @JvmStatic private external fun createToAddress(
            dbDataPath: String,
            consensusBranchId: Long,
            account: Int,
            extsk: String,
            to: String,
            value: Long,
            memo: ByteArray,
            spendParamsPath: String,
            outputParamsPath: String,
            chainNetwork: UShort
        ): Long

        @JvmStatic private external fun initLogs()

        @JvmStatic private external fun branchIdForHeight(height: Int, chainNetwork: UShort): Long

        @JvmStatic private external fun parseTransactionDataList(serializedList: ByteArray): ByteArray
    }
}
