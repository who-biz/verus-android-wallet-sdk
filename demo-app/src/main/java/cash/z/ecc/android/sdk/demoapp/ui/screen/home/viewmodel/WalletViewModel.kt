package cash.z.ecc.android.sdk.demoapp.ui.screen.home.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.SdkSynchronizer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.WalletCoordinator
import cash.z.ecc.android.sdk.WalletInitMode
import cash.z.ecc.android.sdk.block.processor.CompactBlockProcessor
import cash.z.ecc.android.sdk.demoapp.ext.defaultForNetwork
import cash.z.ecc.android.sdk.demoapp.getInstance
import cash.z.ecc.android.sdk.demoapp.preference.EncryptedPreferenceKeys
import cash.z.ecc.android.sdk.demoapp.preference.EncryptedPreferenceSingleton
import cash.z.ecc.android.sdk.demoapp.ui.common.ANDROID_STATE_FLOW_TIMEOUT
import cash.z.ecc.android.sdk.demoapp.ui.common.throttle
import cash.z.ecc.android.sdk.demoapp.util.fromResources
import cash.z.ecc.android.sdk.internal.Twig
import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.BlockHeight
import cash.z.ecc.android.sdk.model.PercentDecimal
import cash.z.ecc.android.sdk.model.PersistableWallet
import cash.z.ecc.android.sdk.model.Proposal
import cash.z.ecc.android.sdk.model.TransactionSubmitResult
import cash.z.ecc.android.sdk.model.WalletAddresses
import cash.z.ecc.android.sdk.model.WalletBalance
import cash.z.ecc.android.sdk.model.Zatoshi
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.model.ZecSend
import cash.z.ecc.android.sdk.model.decodeBase58WithChecksum
import cash.z.ecc.android.sdk.model.decodeHex
import cash.z.ecc.android.sdk.model.proposeSend
import cash.z.ecc.android.sdk.model.send
import cash.z.ecc.android.sdk.tool.DerivationTool
import co.electriccoin.lightwallet.client.model.LightWalletEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

// To make this more multiplatform compatible, we need to remove the dependency on Context
// for loading the preferences.
@Suppress("TooManyFunctions")
class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val walletCoordinator = WalletCoordinator.getInstance(application)

    /*
     * Using the Mutex may be overkill, but it ensures that if multiple calls are accidentally made
     * that they have a consistent ordering.
     */
    private val persistWalletMutex = Mutex()

    /**
     * Synchronizer that is retained long enough to survive configuration changes.
     */
    val synchronizer =
        walletCoordinator.synchronizer.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
            null
        )

    val secretState: StateFlow<SecretState> =
        walletCoordinator.persistableWallet
            .map { persistableWallet ->
                if (null == persistableWallet) {
                    SecretState.None
                } else {
                    SecretState.Ready(persistableWallet)
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                SecretState.Loading
            )

    @OptIn(ExperimentalStdlibApi::class)
    val spendingKey =
        secretState
            .filterIsInstance<SecretState.Ready>()
            .map { it.persistableWallet }
            .map {
                if (null == it.wif) {
                    val empty = byteArrayOf()
                    val hexSeed = it.hexSeed.decodeHex()
                    /*val bip39Seed =
                        withContext(Dispatchers.IO) {
                            Mnemonics.MnemonicCode(it.seedPhrase.joinToString()).toSeed()
                        }*/
                    DerivationTool.getInstance().deriveUnifiedSpendingKey(
                        transparentKey = empty,
                        seed = hexSeed,
                        network = it.network,
                        account = Account.DEFAULT
                    )
                } else {
                    Log.w("WifCheck", "WIF included for this Wallet! Using that instead of bip39!")
                    Log.w("WifCheck", "WIF value: " + it.wif)
                    val decodedWif = it.wif!!.decodeBase58WithChecksum()
                    val decodedTrimmedWif = decodedWif.copyOfRange(1, decodedWif.size)
                    Log.w("WifCheck", "Decoded WIF: " + decodedTrimmedWif.toHexString())
                    /*val bip39Seed =
                        withContext(Dispatchers.IO) {
                            Mnemonics.MnemonicCode(it.seedPhrase.joinToString()).toSeed()
                        }
                    */
                    val hexSeed = it.hexSeed.decodeHex()
                    Log.w("WifCheck", "bip39 calculated seed: " + hexSeed.toHexString())
                    DerivationTool.getInstance().deriveUnifiedSpendingKey(
                        transparentKey = decodedTrimmedWif,
                        seed = hexSeed,
                        network = it.network,
                        account = Account.DEFAULT,
                    )
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                null
            )

    @OptIn(ExperimentalStdlibApi::class)
    val shieldedAddress =
        secretState
            .filterIsInstance<SecretState.Ready>()
            .map { it.persistableWallet }
            .map {
                    val hexSeed = it.hexSeed.decodeHex()
                    DerivationTool.getInstance().deriveShieldedAddress(
                        seed = hexSeed,
                        network = it.network,
                        account = Account.DEFAULT
                    )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                null
            )


    @OptIn(ExperimentalCoroutinesApi::class)
    val walletSnapshot: StateFlow<WalletSnapshot?> =
        synchronizer
            .flatMapLatest {
                if (null == it) {
                    flowOf(null)
                } else {
                    it.toWalletSnapshot()
                }
            }
            .throttle(1.seconds)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                null
            )

    val addresses: StateFlow<WalletAddresses?> =
        synchronizer
            .filterNotNull()
            .map {
                WalletAddresses.new(it)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(ANDROID_STATE_FLOW_TIMEOUT),
                null
            )

    private val mutableSendState = MutableStateFlow<SendState>(SendState.None)

    val sendState: StateFlow<SendState> = mutableSendState

    /**
     * Creates a wallet asynchronously and then persists it.  Clients observe
     * [secretState] to see the side effects.  This would be used for a user creating a new wallet.
     *
     * Although waiting for the wallet to be written and then read back is slower, it is probably
     * safer because it 1. guarantees the wallet is written to disk and 2. has a single source of truth.
     */
    fun persistNewWallet() {
        val application = getApplication<Application>()

        viewModelScope.launch {
            val network = ZcashNetwork.fromResources(application)
            val newWallet =
                PersistableWallet.new(
                    application = application,
                    zcashNetwork = network,
                    endpoint = LightWalletEndpoint.defaultForNetwork(network),
                    walletInitMode = WalletInitMode.NewWallet
                )
            persistWallet(newWallet)
        }
    }

    /**
     * Persists a wallet asynchronously.  Clients observe [secretState]
     * to see the side effects.  This would be used for a user restoring a wallet from a backup.
     */
    fun persistExistingWallet(persistableWallet: PersistableWallet) {
        persistWallet(persistableWallet)
    }

    /**
     * Persists a wallet asynchronously.  Clients observe [secretState] to see the side effects.
     */
    private fun persistWallet(persistableWallet: PersistableWallet) {
        val application = getApplication<Application>()

        viewModelScope.launch {
            val preferenceProvider = EncryptedPreferenceSingleton.getInstance(application)
            persistWalletMutex.withLock {
                EncryptedPreferenceKeys.PERSISTABLE_WALLET.putValue(preferenceProvider, persistableWallet)
            }
        }
    }

    /**
     * Asynchronously sends funds.  Note that two sending operations cannot occur at the same time.
     *
     * Observe the result via [sendState].
     */
    fun send(zecSend: ZecSend) {
        if (sendState.value is SendState.Sending) {
            return
        }

        mutableSendState.value = SendState.Sending

        val synchronizer = synchronizer.value
        if (null != synchronizer) {
            viewModelScope.launch {
                val spendingKey = spendingKey.filterNotNull().first()
                runCatching { synchronizer.send(spendingKey, zecSend) }
                    .onSuccess { mutableSendState.value = SendState.Sent(it.toList()) }
                    .onFailure { mutableSendState.value = SendState.Error(it) }
            }
        } else {
            SendState.Error(IllegalStateException("Unable to send funds because synchronizer is not loaded."))
        }
    }

    /**
     * Synchronously provides proposal object for the given [spendingKey] and [zecSend] objects
     */
    fun getSendProposal(zecSend: ZecSend): Proposal? {
        if (sendState.value is SendState.Sending) {
            return null
        }

        val synchronizer = synchronizer.value

        return if (null != synchronizer) {
            // Calling the proposal API within a blocking coroutine should be fine for the showcase purpose
            runBlocking {
                val spendingKey = spendingKey.filterNotNull().first()
                kotlin.runCatching {
                    synchronizer.proposeSend(spendingKey.account, zecSend)
                }.onFailure {
                    Twig.error(it) { "Failed to get transaction proposal" }
                }.getOrNull()
            }
        } else {
            error("Unable to send funds because synchronizer is not loaded.")
        }
    }

    /**
     * Asynchronously shields transparent funds.  Note that two shielding operations cannot occur at the same time.
     *
     * Observe the result via [sendState].
     */
    fun shieldFunds() {
        if (sendState.value is SendState.Sending) {
            return
        }

        mutableSendState.value = SendState.Sending

        val synchronizer = synchronizer.value
        if (null != synchronizer) {
            viewModelScope.launch {
                val spendingKey = spendingKey.filterNotNull().first()
                kotlin.runCatching {
                    @Suppress("MagicNumber")
                    synchronizer.proposeShielding(spendingKey.account, Zatoshi(100000))?.let {
                        synchronizer.createProposedTransactions(
                            it,
                            spendingKey
                        )
                    }
                }
                    .onSuccess { it?.let { mutableSendState.value = SendState.Sent(it.toList()) } }
                    .onFailure { mutableSendState.value = SendState.Error(it) }
            }
        } else {
            SendState.Error(IllegalStateException("Unable to send funds because synchronizer is not loaded."))
        }
    }

    fun clearSendOrShieldState() {
        mutableSendState.value = SendState.None
    }

    /**
     * This method only has an effect if the synchronizer currently is loaded.
     */
    fun rescanBlockchain() {
        viewModelScope.launch {
            walletCoordinator.rescanBlockchain()
        }
    }

    /**
     * This asynchronously resets the SDK state.  This is non-destructive, as SDK state can be rederived.
     *
     * This could be used as a troubleshooting step in debugging.
     */
    fun resetSdk() {
        walletCoordinator.resetSdk()
    }

    /**
     * This rewinds to the nearest height, i.e. 14 days back from the current chain tip.
     */
    fun rewind() {
        val synchronizer = synchronizer.value
        if (null != synchronizer) {
            viewModelScope.launch {
                synchronizer.quickRewind()
            }
        }
    }

    /**
     * This safely and asynchronously stops the [Synchronizer].
     */
    fun closeSynchronizer() {
        val synchronizer = synchronizer.value
        if (null != synchronizer) {
            viewModelScope.launch {
                (synchronizer as SdkSynchronizer).close()
            }
        }
    }
}

/**
 * Represents the state of the wallet secret.
 */
sealed class SecretState {
    object Loading : SecretState()

    object None : SecretState()

    class Ready(val persistableWallet: PersistableWallet) : SecretState()
}

sealed class SendState {
    object None : SendState() {
        override fun toString(): String = "None"
    }

    object Sending : SendState() {
        override fun toString(): String = "Sending"
    }

    class Sent(val txIds: List<TransactionSubmitResult>) : SendState() {
        override fun toString(): String = "Sent"
    }

    class Error(val error: Throwable) : SendState() {
        override fun toString(): String = "Error ${error.message}"
    }
}

// TODO [#529]: Localize Synchronizer Errors
// TODO [#529]: https://github.com/zcash/secant-android-wallet/issues/529

/**
 * Represents all kind of Synchronizer errors
 */

sealed class SynchronizerError {
    abstract fun getCauseMessage(): String?

    class Critical(val error: Throwable?) : SynchronizerError() {
        override fun getCauseMessage(): String? = error?.localizedMessage
    }

    class Processor(val error: Throwable?) : SynchronizerError() {
        override fun getCauseMessage(): String? = error?.localizedMessage
    }

    class Submission(val error: Throwable?) : SynchronizerError() {
        override fun getCauseMessage(): String? = error?.localizedMessage
    }

    class Setup(val error: Throwable?) : SynchronizerError() {
        override fun getCauseMessage(): String? = error?.localizedMessage
    }

    class Chain(val x: BlockHeight, val y: BlockHeight) : SynchronizerError() {
        override fun getCauseMessage(): String = "$x, $y"
    }
}

private fun Synchronizer.toCommonError(): Flow<SynchronizerError?> =
    callbackFlow {
        // just for initial default value emit
        trySend(null)

        onCriticalErrorHandler = {
            Twig.error { "WALLET - Error Critical: $it" }
            trySend(SynchronizerError.Critical(it))
            false
        }
        onProcessorErrorHandler = {
            Twig.error { "WALLET - Error Processor: $it" }
            trySend(SynchronizerError.Processor(it))
            false
        }
        onSubmissionErrorHandler = {
            Twig.error { "WALLET - Error Submission: $it" }
            trySend(SynchronizerError.Submission(it))
            false
        }
        onSetupErrorHandler = {
            Twig.error { "WALLET - Error Setup: $it" }
            trySend(SynchronizerError.Setup(it))
            false
        }
        onChainErrorHandler = { x, y ->
            Twig.error { "WALLET - Error Chain: $x, $y" }
            trySend(SynchronizerError.Chain(x, y))
        }

        awaitClose {
            // nothing to close here
        }
    }

// No good way around needing magic numbers for the indices
@Suppress("MagicNumber")
private fun Synchronizer.toWalletSnapshot() =
    combine(
        // 0
        status,
        // 1
        processorInfo,
        // 2
        orchardBalances,
        // 3
        saplingBalances,
        // 4
        transparentBalance,
        // 5
        progress,
        // 6
        toCommonError()
    ) { flows ->
        val orchardBalance = flows[2] as WalletBalance?
        val saplingBalance = flows[3] as WalletBalance?
        val transparentBalance = flows[4] as Zatoshi?
        val progressPercentDecimal = (flows[5] as PercentDecimal)

        WalletSnapshot(
            flows[0] as Synchronizer.Status,
            flows[1] as CompactBlockProcessor.ProcessorInfo,
            orchardBalance ?: WalletBalance(Zatoshi(0), Zatoshi(0), Zatoshi(0)),
            saplingBalance ?: WalletBalance(Zatoshi(0), Zatoshi(0), Zatoshi(0)),
            transparentBalance ?: Zatoshi(0),
            progressPercentDecimal,
            flows[6] as SynchronizerError?
        )
    }
