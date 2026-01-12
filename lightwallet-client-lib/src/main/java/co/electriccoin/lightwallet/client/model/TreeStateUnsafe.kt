package co.electriccoin.lightwallet.client.model

import cash.z.wallet.sdk.internal.rpc.Service.TreeState

class TreeStateUnsafe(
    val encoded: ByteArray
) {
    companion object {
        fun new(treeState: TreeState): TreeStateUnsafe {
            return TreeStateUnsafe(treeState.toByteArray())
        }

        fun fromParts(
            height: Long,
            hash: String,
            time: Int,
            tree: String,
//            orchardTree: String
        ): TreeStateUnsafe {
            val treeState =
                TreeState.newBuilder()
                    .setHeight(height)
                    .setHash(hash)
                    .setTime(time)
                    .setTree(tree)
//                    .setOrchardTree(orchardTree)
                    .build()
            return new(treeState)
        }
    }
}
