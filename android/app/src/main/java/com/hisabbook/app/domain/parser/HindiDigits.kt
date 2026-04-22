package com.hisabbook.app.domain.parser

/** Hindi + Hinglish number words → numeric value.
 *  Handles "do sau", "teen hazaar paanch sau", "ek lakh", etc.
 *  Also accepts Devanagari digits (०-९) and Arabic digits. */
object HindiDigits {

    private val ones: Map<String, Int> = mapOf(
        "zero" to 0, "shunya" to 0, "ek" to 1, "do" to 2, "teen" to 3, "tin" to 3, "char" to 4,
        "chaar" to 4, "paanch" to 5, "panch" to 5, "chhe" to 6, "che" to 6, "chhah" to 6, "saat" to 7,
        "sat" to 7, "aath" to 8, "ath" to 8, "nau" to 9, "no" to 9,
        "das" to 10, "gyarah" to 11, "egara" to 11, "baarah" to 12, "barah" to 12,
        "terah" to 13, "chaudah" to 14, "pandrah" to 15, "solah" to 16, "satrah" to 17,
        "atharah" to 18, "unnis" to 19, "bees" to 20, "ikkis" to 21, "baais" to 22, "taees" to 23,
        "chaubis" to 24, "pachees" to 25, "pachchis" to 25, "chhabbis" to 26, "sattais" to 27,
        "atthais" to 28, "untees" to 29, "tees" to 30, "pachas" to 50, "pachaas" to 50,
        "saath" to 60, "sattar" to 70, "assi" to 80, "nabbe" to 90
    )

    private val scales: Map<String, Long> = mapOf(
        "sau" to 100L, "hundred" to 100L,
        "hazaar" to 1_000L, "hazar" to 1_000L, "thousand" to 1_000L, "k" to 1_000L,
        "lakh" to 1_00_000L, "lac" to 1_00_000L, "lakhs" to 1_00_000L,
        "crore" to 1_00_00_000L, "cr" to 1_00_00_000L, "karod" to 1_00_00_000L
    )

    private val devanagariDigits = "०१२३४५६७८९"

    /** Scan text, extract first monetary amount. Returns rupees (Long), null if none. */
    fun extractAmount(text: String): Long? {
        val normalized = normalize(text)

        // 1. Arabic digit run with optional scale word
        Regex("""(\d[\d,]*)(\s*(sau|hazaar|hazar|thousand|lakh|lac|crore|cr|k))?""").find(normalized)?.let { m ->
            val num = m.groupValues[1].replace(",", "").toLongOrNull() ?: return@let
            val scaleWord = m.groupValues[3].takeIf { it.isNotBlank() }
            val scale = scaleWord?.let { scales[it] } ?: 1L
            return num * scale
        }

        // 2. Devanagari digits
        val devMatch = Regex("[०-९]+").find(normalized)
        if (devMatch != null) {
            val asArabic = devMatch.value.map { devanagariDigits.indexOf(it) }.joinToString("")
            return asArabic.toLongOrNull()
        }

        // 3. Word form: tokenize and fold.
        val tokens = normalized.split(Regex("""[^a-z]+""")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null

        var total = 0L
        var current = 0L
        var sawAny = false

        for (tok in tokens) {
            val one = ones[tok]
            val scale = scales[tok]
            when {
                one != null -> {
                    current += one
                    sawAny = true
                }
                scale != null -> {
                    if (current == 0L) current = 1L
                    current *= scale
                    if (scale >= 1_000L) {
                        total += current
                        current = 0L
                    }
                    sawAny = true
                }
                else -> { /* skip unknown token */ }
            }
        }
        total += current
        return if (sawAny && total > 0) total else null
    }

    internal fun normalize(text: String): String =
        text.lowercase()
            .replace("₹", " ")
            .replace("rs", " ")
            .replace("rupees", " ")
            .replace("rupaye", " ")
            .replace("rupay", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
