package com.hisabbook.app.domain.parser

import com.hisabbook.app.data.model.EntryType

data class ParsedEntry(
    val type: EntryType?,
    val person: String?,
    val amountPaise: Long?,
    val item: String?,
    val note: String?,
    val confidence: Float
)

/** Pure-Kotlin rule parser for shopkeeper utterances in Hindi/Hinglish.
 *  Input = ASR transcript. Output = ParsedEntry with chips; fields may be null.
 *  Confidence is 0..1 based on how many fields resolved. */
object RulesParser {

    // Stopwords (filler + common connectors) — these are NEVER the person name or item
    private val stopwords = setOf(
        "ko", "ka", "ki", "ke", "se", "mein", "me", "par", "pe", "aur", "or", "hai", "tha", "thi",
        "kal", "aaj", "parson", "pichle", "agle", "naya", "purana",
        "udhar", "udhaar", "diya", "liya", "mila", "mile", "chuka",
        "sau", "hazaar", "hazar", "lakh", "crore", "k", "thousand",
        "ek", "do", "teen", "char", "chaar", "paanch", "panch", "chhe", "saat", "aath", "nau",
        "das", "bees", "tees", "chalis", "pachas", "pachaas", "saath", "sattar", "assi", "nabbe",
        "rupaye", "rupay", "rs",
        "bikri", "kharch", "kharcha", "expense", "becha", "jama",
        "ka", "ki",
        "wapas", "mil", "milegi", "milega",
        "naam", "rakam", "kaam", "cheez"
    )

    // Common Hindi item nouns that should be recognised even if capitalization is missing
    private val commonItems = setOf(
        "doodh", "dudh", "chai", "chai patti", "patti", "ration", "aloo", "pyaz", "tomato",
        "sabzi", "namak", "chawal", "atta", "daal", "dal", "ghee", "tel", "cheeni", "chini",
        "biscuit", "biscuits", "paani", "pan", "masala", "maida", "roti", "dhaniya",
        "sugar", "bread", "egg", "milk"
    )

    fun parse(transcript: String): ParsedEntry {
        val normalized = transcript.lowercase().trim()
        val amount = HindiDigits.extractAmount(normalized)
        val type = VerbMap.detectType(normalized)

        val tokens = transcript.split(Regex("""\s+""")).filter { it.isNotEmpty() }
        val person = extractPerson(tokens)
        val item = extractItem(tokens, person)
        val note = extractNote(normalized)

        val fieldCount = listOf(type, person, amount, item).count { it != null }
        val confidence = fieldCount / 4f

        return ParsedEntry(
            type = type,
            person = person,
            amountPaise = amount?.times(100L),
            item = item,
            note = note,
            confidence = confidence
        )
    }

    /** First proper-noun-shaped token that's not a stopword. */
    private fun extractPerson(tokens: List<String>): String? {
        // Pass 1: capitalized non-stopword
        for (t in tokens) {
            val clean = t.trim('.', ',', '\"', '\'', '!', '?')
            if (clean.isEmpty()) continue
            if (clean.first().isUpperCase() && clean.lowercase() !in stopwords && clean.lowercase() !in commonItems) {
                if (clean.any { it.isLetter() }) return clean
            }
        }
        // Pass 2: any non-stopword that's letters-only and not numeric word
        for (t in tokens) {
            val clean = t.lowercase().trim('.', ',', '\"', '\'', '!', '?')
            if (clean.isEmpty() || clean.any { !it.isLetter() }) continue
            if (clean in stopwords || clean in commonItems) continue
            if (HindiDigits.extractAmount(clean) != null) continue
            if (VerbMap.detectType(clean) != null) continue
            return clean.replaceFirstChar { it.uppercaseChar() }
        }
        return null
    }

    /** Find item noun — prefer known dict match; else last non-stopword token after number. */
    private fun extractItem(tokens: List<String>, person: String?): String? {
        val lowered = tokens.map { it.lowercase().trim('.', ',', '\"', '\'', '!', '?') }
        for (w in lowered) {
            if (w in commonItems) return w
        }
        // fallback: pick any non-stopword, non-person, non-numeric letters-only token
        for (w in lowered) {
            if (w.isEmpty() || w.any { !it.isLetter() }) continue
            if (w in stopwords) continue
            if (person != null && w == person.lowercase()) continue
            if (HindiDigits.extractAmount(w) != null) continue
            return w
        }
        return null
    }

    /** Extract time hint (kal, aaj, parson) as note; else null. */
    private fun extractNote(text: String): String? {
        return when {
            "kal ka" in text || "kal ki" in text -> "kal ka"
            " kal " in " $text " -> "kal"
            "aaj" in text -> null // today is default
            "parson" in text -> "parson"
            else -> null
        }
    }
}
