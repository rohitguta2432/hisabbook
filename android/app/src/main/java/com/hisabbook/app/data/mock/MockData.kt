package com.hisabbook.app.data.mock

import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.data.model.Source

object MockData {

    val persons = listOf(
        Person(id = "p1", name = "Ramesh", phone = "+91 98765 43210", type = PersonType.CUSTOMER, balancePaise = 5_40_000L),
        Person(id = "p2", name = "Sunil Bhai", phone = "+91 98000 12345", type = PersonType.CUSTOMER, balancePaise = 1_20_000L),
        Person(id = "p3", name = "Priya Didi", phone = "+91 97111 44556", type = PersonType.CUSTOMER, balancePaise = 0L),
        Person(id = "p4", name = "Rajesh Chacha", phone = "+91 98989 00011", type = PersonType.CUSTOMER, balancePaise = 8_50_000L),
        Person(id = "p5", name = "Sabzi Wala", phone = "+91 99123 77889", type = PersonType.SUPPLIER, balancePaise = -2_80_000L),
        Person(id = "p6", name = "Anita", phone = null, type = PersonType.CUSTOMER, balancePaise = 45_000L)
    )

    val ramesh = persons[0]

    val entries = listOf(
        Entry("e1", EntryType.UDHAR_DIYA, "p1", "Ramesh", 20_000L, "Doodh", null, "hi", dayAt(22, 4, 2026), Source.VOICE_LLM),
        Entry("e2", EntryType.UDHAR_DIYA, "p1", "Ramesh", 1_20_000L, "Ration", null, "hi", dayAt(20, 4, 2026), Source.VOICE_LLM),
        Entry("e3", EntryType.UDHAR_WAPAS, "p1", "Ramesh", 1_00_000L, "Jama Kiya", null, "hi", dayAt(15, 4, 2026), Source.MANUAL),
        Entry("e4", EntryType.UDHAR_DIYA, "p2", "Sunil Bhai", 1_20_000L, "Chai patti", null, "hi", dayAt(18, 4, 2026), Source.MANUAL),
        Entry("e5", EntryType.UDHAR_LIYA, "p5", "Sabzi Wala", 2_80_000L, "Aloo pyaz", null, "hi", dayAt(21, 4, 2026), Source.VOICE_VOSK),
        Entry("e6", EntryType.BIKRI, null, null, 45_000L, "Chai 30", null, "hi", dayAt(22, 4, 2026), Source.MANUAL),
        Entry("e7", EntryType.KHARCH, null, null, 12_000L, "Bijli", null, "hi", dayAt(22, 4, 2026), Source.MANUAL)
    )

    fun rameshEntries() = entries.filter { it.personId == "p1" }

    private fun dayAt(d: Int, m: Int, y: Int): Long {
        val c = java.util.Calendar.getInstance()
        c.set(y, m - 1, d, 12, 0, 0)
        return c.timeInMillis
    }
}
