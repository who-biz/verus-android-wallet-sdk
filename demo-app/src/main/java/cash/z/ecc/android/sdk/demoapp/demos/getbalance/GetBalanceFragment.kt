package cash.z.ecc.android.sdk.demoapp.demos.getbalance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.demoapp.BaseDemoFragment
import cash.z.ecc.android.sdk.demoapp.databinding.FragmentGetBalanceBinding
import cash.z.ecc.android.sdk.demoapp.ext.requireApplicationContext
import cash.z.ecc.android.sdk.demoapp.util.SyncBlockchainBenchmarkTrace
import cash.z.ecc.android.sdk.demoapp.util.fromResources
import cash.z.ecc.android.sdk.ext.convertZatoshiToZecString
import cash.z.ecc.android.sdk.internal.Twig
import cash.z.ecc.android.sdk.model.Account
import cash.z.ecc.android.sdk.model.PercentDecimal
import cash.z.ecc.android.sdk.model.WalletBalance
import cash.z.ecc.android.sdk.model.Zatoshi
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.model.decodeBase58WithChecksum
import cash.z.ecc.android.sdk.tool.DerivationTool
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Displays the available balance && total balance associated with the seed defined by the default config.
 * comments.
 */
@Suppress("TooManyFunctions")
class GetBalanceFragment : BaseDemoFragment<FragmentGetBalanceBinding>() {
    override fun inflateBinding(layoutInflater: LayoutInflater): FragmentGetBalanceBinding =
        FragmentGetBalanceBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportTraceEvent(SyncBlockchainBenchmarkTrace.Event.BALANCE_SCREEN_START)
    }

    override fun onDestroy() {
        super.onDestroy()
        reportTraceEvent(SyncBlockchainBenchmarkTrace.Event.BALANCE_SCREEN_END)
    }

    @Suppress("MagicNumber")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val seedPhrase = sharedViewModel.seedPhrase.value
        val seed = Mnemonics.MnemonicCode(seedPhrase).toSeed()

        val wifString = sharedViewModel.wifString.value
        val decodedWif = wifString.decodeBase58WithChecksum()
        val transparentKey = decodedWif.copyOfRange(1, decodedWif.size)

        val network = ZcashNetwork.fromResources(requireApplicationContext())

        binding.shield.apply {
            setOnClickListener {
                lifecycleScope.launch {
                    val usk =
                        DerivationTool.getInstance().deriveUnifiedSpendingKey(
                            transparentKey,
                            seed,
                            network,
                            Account.DEFAULT
                        )
                    sharedViewModel.synchronizerFlow.value?.let { synchronizer ->
                        synchronizer.proposeShielding(usk.account, Zatoshi(100000))?.let { it1 ->
                            synchronizer.createProposedTransactions(
                                it1,
                                usk
                            )
                        }
                    }
                }
            }
        }

        monitorChanges()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun monitorChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    sharedViewModel.synchronizerFlow
                        .filterNotNull()
                        .flatMapLatest { it.status }
                        .collect { onStatus(it) }
                }
                launch {
                    sharedViewModel.synchronizerFlow
                        .filterNotNull()
                        .flatMapLatest { it.progress }
                        .collect { onProgress(it) }
                }
                launch {
                    sharedViewModel.synchronizerFlow
                        .filterNotNull()
                        .flatMapLatest { it.saplingBalances }
                        .collect { onSaplingBalance(it) }
                }
                launch {
                    sharedViewModel.synchronizerFlow
                        .filterNotNull()
                        .flatMapLatest { it.orchardBalances }
                        .collect { onOrchardBalance(it) }
                }
                launch {
                    sharedViewModel.synchronizerFlow
                        .filterNotNull()
                        .flatMapLatest { it.transparentBalance }
                        .collect { onTransparentBalance(it) }
                }
            }
        }
    }

    private fun onOrchardBalance(orchardBalance: WalletBalance?) {
        binding.orchardBalance.apply {
            text = orchardBalance.humanString()
        }
    }

    private fun onSaplingBalance(saplingBalance: WalletBalance?) {
        binding.saplingBalance.apply {
            text = saplingBalance.humanString()
        }
    }

    private fun onTransparentBalance(transparentBalance: Zatoshi?) {
        binding.transparentBalance.apply {
            text = transparentBalance.humanString()
        }

        binding.shield.apply {
            // This check is not entirely correct - it does not calculate the resulting fee with the new Proposal API
            // Note that the entire fragment-based old Demo app will be removed as part of [#973]
            visibility =
                if ((transparentBalance ?: Zatoshi(0)).value > 0L) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun onStatus(status: Synchronizer.Status) {
        Twig.debug { "Synchronizer status: $status" }
        // report benchmark event
        val traceEvents =
            when (status) {
                Synchronizer.Status.SYNCING -> {
                    SyncBlockchainBenchmarkTrace.Event.BLOCKCHAIN_SYNC_START
                }
                Synchronizer.Status.SYNCED -> {
                    SyncBlockchainBenchmarkTrace.Event.BLOCKCHAIN_SYNC_END
                }
                else -> null
            }
        traceEvents?.let { reportTraceEvent(it) }

        binding.textStatus.text = "Status: $status"
        sharedViewModel.synchronizerFlow.value?.let { synchronizer ->
            onOrchardBalance(synchronizer.orchardBalances.value)
            onSaplingBalance(synchronizer.saplingBalances.value)
            onTransparentBalance(synchronizer.transparentBalance.value)
        }
    }

    @Suppress("MagicNumber")
    private fun onProgress(percent: PercentDecimal) {
        if (percent.isLessThanHundredPercent()) {
            binding.textStatus.text = "Syncing blocks...${percent.toPercentage()}%"
        }
    }
}

@Suppress("MagicNumber")
private fun WalletBalance?.humanString() =
    if (null == this) {
        "Calculating balance"
    } else {
        """
        Pending balance: ${pending.convertZatoshiToZecString(12)}
        Available balance: ${available.convertZatoshiToZecString(12)}
        Total balance: ${total.convertZatoshiToZecString(12)}
        """.trimIndent()
    }

@Suppress("MagicNumber")
private fun Zatoshi?.humanString() =
    if (null == this) {
        "Calculating balance"
    } else {
        """
        Balance: ${convertZatoshiToZecString(12)}
        """.trimIndent()
    }
