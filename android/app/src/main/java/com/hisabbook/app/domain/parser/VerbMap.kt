package com.hisabbook.app.domain.parser

import com.hisabbook.app.data.model.EntryType

/** Keywords and phrases mapped to entry types. Order matters — longer/more-specific first. */
object VerbMap {

    private data class Rule(val keywords: List<String>, val type: EntryType)

    private val rules = listOf(
        // UDHAR_CHUKAYA — I paid supplier
        Rule(listOf("udhar chukaya", "chuka diya", "loan wapas kiya", "supplier ko diya"), EntryType.UDHAR_CHUKAYA),
        // UDHAR_LIYA — I took from supplier on credit
        Rule(listOf("udhar liya", "loan liya", "se udhaar", "se udhar"), EntryType.UDHAR_LIYA),
        // UDHAR_WAPAS — customer paid me
        Rule(listOf("paisa wapas", "paise wapas", "payment mili", "paisa mila", "jama kiya", "jama hua", "udhar wapas", "wapas diya", "se mila", "se mile"), EntryType.UDHAR_WAPAS),
        // UDHAR_DIYA — I gave on credit to customer
        Rule(listOf("udhar diya", "udhaar diya", "ko udhar", "ko udhaar", "credit diya"), EntryType.UDHAR_DIYA),
        // KHARCH — expense
        Rule(listOf("kharch", "kharcha", "expense", "spend", "bijli", "rent", "bhatta", "kiraya"), EntryType.KHARCH),
        // BIKRI — sale
        Rule(listOf("bikri", "becha", "sell", "bik gaya", "sold"), EntryType.BIKRI)
    )

    fun detectType(text: String): EntryType? {
        val t = text.lowercase()
        for (rule in rules) {
            for (kw in rule.keywords) {
                if (t.contains(kw)) return rule.type
            }
        }
        // weaker fallback
        return when {
            "wapas" in t -> EntryType.UDHAR_WAPAS
            "aaye" in t && "udhar" !in t -> EntryType.UDHAR_WAPAS
            "mila" in t || "mile" in t -> EntryType.UDHAR_WAPAS
            "udhar" in t || "udhaar" in t -> EntryType.UDHAR_DIYA
            "diya" in t -> EntryType.UDHAR_DIYA
            else -> null
        }
    }
}
