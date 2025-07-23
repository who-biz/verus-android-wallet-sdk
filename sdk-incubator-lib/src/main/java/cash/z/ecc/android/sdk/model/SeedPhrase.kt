package cash.z.ecc.android.sdk.model

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

// Consider using ImmutableList here
data class SeedPhrase(val split: List<String>) {
    init {
        if (SEED_PHRASE_SIZE != split.size) {
            require(HEX_SEED_SIZE == split.size) {
                "You must either use a Seed Phrase of $SEED_PHRASE_SIZE words, or a $HEX_SEED_SIZE word hex value, but was ${split.size}"
            }
        } else {
            require(SEED_PHRASE_SIZE == split.size) {
                "Seed phrase must split into $SEED_PHRASE_SIZE words but was ${split.size}"
            }
        }
    }

    // For security, intentionally override the toString method to reduce risk of accidentally logging secrets
    override fun toString() = "SeedPhrase"

    fun joinToString() = split.joinToString(DEFAULT_DELIMITER)

    suspend fun toByteArray() = withContext(Dispatchers.IO) { 
        if (HEX_SEED_SIZE == split.size) {
            Log.w("SeedPhrase", "Seed value: $split.first()" ); 
            split.first().decodeHex()            
        } else {
            Mnemonics.MnemonicCode(joinToString()).toSeed()
        } 
    }

    companion object {
        const val SEED_PHRASE_SIZE = 24

        const val HEX_SEED_SIZE = 1

        const val DEFAULT_DELIMITER = " "

        fun new(phrase: String) = SeedPhrase(phrase.split(DEFAULT_DELIMITER))
    }
}

// from https://stackoverflow.com/questions/66613717/kotlin-convert-hex-string-to-bytearray
fun String.decodeHex(): ByteArray {
    require(length % 2 == 0) { "Must have an even length" }

    val byteIterator = chunkedSequence(2)
        .map { it.toInt(16).toByte() }
        .iterator()

    return ByteArray(length / 2) { byteIterator.next() }
}
