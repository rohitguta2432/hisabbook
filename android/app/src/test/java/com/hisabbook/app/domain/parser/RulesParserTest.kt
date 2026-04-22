package com.hisabbook.app.domain.parser

import com.hisabbook.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesParserTest {

    @Test fun `ramesh udhar doodh example parses all fields`() {
        val r = RulesParser.parse("Ramesh ko 200 ka doodh udhar diya")
        assertEquals(EntryType.UDHAR_DIYA, r.type)
        assertEquals("Ramesh", r.person)
        assertEquals(20_000L, r.amountPaise) // 200 rupees = 20000 paise
        assertEquals("doodh", r.item)
        assertTrue(r.confidence >= 0.75f)
    }

    @Test fun `word form digits work`() {
        val r = RulesParser.parse("Sunil ko do sau ka chai patti udhar diya")
        assertEquals(EntryType.UDHAR_DIYA, r.type)
        assertEquals("Sunil", r.person)
        assertEquals(20_000L, r.amountPaise)
    }

    @Test fun `wapas detected`() {
        val r = RulesParser.parse("Ramesh se 500 wapas aaye")
        assertEquals(EntryType.UDHAR_WAPAS, r.type)
        assertEquals(50_000L, r.amountPaise)
    }

    @Test fun `kharch no person`() {
        val r = RulesParser.parse("bijli ka kharch 300")
        assertEquals(EntryType.KHARCH, r.type)
        assertEquals(30_000L, r.amountPaise)
    }

    @Test fun `kal note extracted`() {
        val r = RulesParser.parse("Ramesh ko 200 udhar diya kal ka")
        assertEquals("kal ka", r.note)
    }

    @Test fun `empty returns nulls`() {
        val r = RulesParser.parse("")
        assertEquals(null, r.type)
        assertEquals(null, r.amountPaise)
    }

    @Test fun `confidence scales with filled fields`() {
        val full = RulesParser.parse("Ramesh ko 200 doodh udhar diya")
        val partial = RulesParser.parse("200 diya")
        assertTrue(full.confidence > partial.confidence)
    }
}
