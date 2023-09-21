package cash.z.ecc.android.sdk.demoapp.ui.screen.seed.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cash.z.ecc.android.sdk.WalletInitMode
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.demoapp.util.fromResources
import cash.z.ecc.android.sdk.fixture.WalletFixture
import cash.z.ecc.android.sdk.model.BlockHeight
import cash.z.ecc.android.sdk.model.PersistableWallet
import cash.z.ecc.android.sdk.model.SeedPhrase
import cash.z.ecc.android.sdk.model.ZcashNetwork
import cash.z.ecc.android.sdk.model.defaultForNetwork
import co.electriccoin.lightwallet.client.model.LightWalletEndpoint

@Preview(name = "Seed")
@Composable
private fun ComposablePreview() {
    MaterialTheme {
        Seed(
            ZcashNetwork.Mainnet,
            onExistingWallet = {},
            onNewWallet = {}
        )
    }
}

@Composable
fun Seed(
    zcashNetwork: ZcashNetwork,
    onExistingWallet: (PersistableWallet) -> Unit,
    onNewWallet: () -> Unit
) {
    Scaffold(topBar = {
        ConfigureSeedTopAppBar()
    }) { paddingValues ->
        ConfigureSeedMainContent(
            paddingValues = paddingValues,
            zcashNetwork = zcashNetwork,
            onExistingWallet = onExistingWallet,
            onNewWallet = onNewWallet
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConfigureSeedTopAppBar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.configure_seed)) }
    )
}

@Composable
private fun ConfigureSeedMainContent(
    paddingValues: PaddingValues,
    zcashNetwork: ZcashNetwork,
    onExistingWallet: (PersistableWallet) -> Unit,
    onNewWallet: () -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        Button(
            onClick = {
                val newWallet = PersistableWallet(
                    network = zcashNetwork,
                    endpoint = LightWalletEndpoint.defaultForNetwork(zcashNetwork),
                    birthday = WalletFixture.Alice.getBirthday(zcashNetwork),
                    seedPhrase = SeedPhrase.new(WalletFixture.Alice.seedPhrase),
                    walletInitMode = WalletInitMode.RestoreWallet
                )
                onExistingWallet(newWallet)
            }
        ) {
            Text(text = stringResource(id = R.string.person_alyssa))
        }
        Button(
            onClick = {
                val newWallet = PersistableWallet(
                    network = zcashNetwork,
                    endpoint = LightWalletEndpoint.defaultForNetwork(zcashNetwork),
                    birthday = WalletFixture.Ben.getBirthday(zcashNetwork),
                    seedPhrase = SeedPhrase.new(WalletFixture.Ben.seedPhrase),
                    walletInitMode = WalletInitMode.RestoreWallet
                )
                onExistingWallet(newWallet)
            }
        ) {
            Text(text = stringResource(R.string.person_ben))
        }
        Button(
            onClick = onNewWallet
        ) {
            Text(text = stringResource(id = R.string.seed_random))
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(text = stringResource(R.string.seed_custom_title))

        // TODO seed text field

        val (height, setHeight) = rememberSaveable {
            mutableStateOf("")
        }

        TextField(
            value = height,
            onValueChange = { heightString ->
                val filteredHeightString = heightString.filter { it.isDigit() }
                setHeight(filteredHeightString)
            },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.seed_custom_birthday_height)) },
            keyboardOptions = KeyboardOptions(
                KeyboardCapitalization.None,
                autoCorrect = false,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(onAny = {}),
            shape = RectangleShape,
        )

        val network = ZcashNetwork.fromResources(LocalContext.current)
        Button(
            onClick = {

                val newWallet = PersistableWallet(
                    zcashNetwork,
                    BlockHeight.new(network, height.toLong()),
                    SeedPhrase.new(WalletFixture.Ben.seedPhrase),
                    WalletInitMode.RestoreWallet
                )
                onExistingWallet(newWallet)
            }
        ) {
            Text(text = stringResource(id = R.string.seed_random))
        }
    }
}
