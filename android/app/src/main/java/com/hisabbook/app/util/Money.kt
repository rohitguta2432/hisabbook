package com.hisabbook.app.util

import kotlin.math.abs

/** Format paise as Indian lakh/crore with ₹ prefix. */
fun Long.toRupeesString(short: Boolean = false): String {
    val paise = this
    val rupees = paise / 100
    val neg = rupees < 0
    val abs = abs(rupees)
    val body = if (short) formatShort(abs) else formatIndian(abs)
    return (if (neg) "-" else "") + "₹" + body
}

/** "1,50,000" Indian-style grouping. */
fun formatIndian(value: Long): String {
    if (value < 1000) return value.toString()
    val s = value.toString()
    val lastThree = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$lastThree"
}

/** "1.5 Lakh", "2 Cr", etc. */
fun formatShort(value: Long): String {
    return when {
        value >= 1_00_00_000L -> {
            val cr = value / 1_00_00_000.0
            "%.1f Cr".format(cr).trimEnd('0').trimEnd('.').let { if (!it.contains(".")) "$it Cr" else "$it Cr" }
        }
        value >= 1_00_000L -> {
            val l = value / 1_00_000.0
            "%.1f Lakh".format(l).replace(".0 Lakh", " Lakh")
        }
        value >= 1_000L -> {
            val k = value / 1000.0
            "%.1fK".format(k).replace(".0K", "K")
        }
        else -> value.toString()
    }
}
