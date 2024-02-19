package cash.z.ecc.android.sdk.model

/**
 * A result object for a transaction that was created as part of a proposal, indicating
 * whether it was submitted to the network or if an error occurred.
 */
sealed class TransactionSubmitResult {
    data class Success(val txId: FirstClassByteArray) : TransactionSubmitResult()
    data class Failure(
        val txId: FirstClassByteArray,
        val grpcError: Boolean,
        val code: Int,
        val description: String?
    ) : TransactionSubmitResult()
    data class NotAttempted(val txId: FirstClassByteArray) : TransactionSubmitResult()
}
