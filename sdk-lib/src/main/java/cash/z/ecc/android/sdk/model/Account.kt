package cash.z.ecc.android.sdk.model

/**
 * A [ZIP 316](https://zips.z.cash/zip-0316) account identifier.
 *
 * @param value A 0-based account index.  Must be >= 0.
 */
data class Account(val value: Int) {
    init {
        require(value >= -1) { "Account index must be >= 0 if valid, or >= -1 if a placeholder for AccountSource::Imported. Attempted account index was $value" }
    }

    companion object {
        val DEFAULT = Account(0)
    }
}
