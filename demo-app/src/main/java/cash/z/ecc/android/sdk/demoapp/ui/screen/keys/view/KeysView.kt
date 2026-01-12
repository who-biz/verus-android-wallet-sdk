package cash.z.ecc.android.sdk.demoapp.ui.screen.keys.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.demoapp.demos.getprivatekey.GetPrivateKeyFragment
import cash.z.ecc.android.sdk.model.PersistableWallet
import cash.z.ecc.android.sdk.model.UnifiedSpendingKey
import cash.z.ecc.android.sdk.model.decodeBase58WithChecksum

@Preview(name = "Keys")
@Composable
@Suppress("ktlint:standard:function-naming")
private fun ComposablePreview() {
    MaterialTheme {
        // TODO [#1090]: Demo: Add Addresses and Transactions Compose Previews
        // TODO [#1090]: https://github.com/zcash/zcash-android-wallet-sdk/issues/1090
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun Keys(
    persistableWallet: PersistableWallet,
    spendingKey: UnifiedSpendingKey,
    shieldedAddress: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { KeysTopAppBar(onBack) },
        //snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        KeysMainContent(
            paddingValues = paddingValues,
            persistableWallet = persistableWallet,
            spendingKey = spendingKey,
            shieldedAddress = shieldedAddress
            )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
private fun KeysTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.menu_private_key)) },
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun KeysMainContent(
    paddingValues: PaddingValues,
    persistableWallet: PersistableWallet,
    spendingKey: UnifiedSpendingKey,
    shieldedAddress: String
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = paddingValues.calculateTopPadding())
    ) {

        Spacer(Modifier.padding(8.dp))
        Text(stringResource(id = R.string.shielded_address))
        //Text(spendingKey.toString())
        // note: above value is overridden with placeholder, for security reasons
        Text(shieldedAddress)


        Spacer(Modifier.padding(8.dp))
        Text(stringResource(id = R.string.spending_key))
        //Text(spendingKey.toString())
        // note: above value is overridden with placeholder, for security reasons
        Text(spendingKey. copyBytes().toHexString())

        Spacer(Modifier.padding(8.dp))

        Text(stringResource(id = R.string.base58_wif))
        persistableWallet.wif.also { wif ->
            if (wif != null) {
                Text(wif)
            } else {
                Text("None Imported")
            }
        }

        Spacer(Modifier.padding(8.dp))

        Text(stringResource(id = R.string.decoded_wif))
        persistableWallet.wif.also { wif ->
            if (wif != null) {
                Text(wif.decodeBase58WithChecksum().toHexString(1,34))
            } else {
                Text("None Imported")
            }
        }

        Spacer(Modifier.padding(8.dp))

        Text(stringResource(id = R.string.seed))
        persistableWallet.hexSeed.also { hexSeed ->
            Text(hexSeed)
        }
        /*persistableWallet.seedPhrase.also { seed ->
                Text(seed.joinToString())
        }*/
    }
}
