package com.hisabbook.app.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HindiDigitsTest {

    @Test fun `do sau = 200`() = assertEquals(200L, HindiDigits.extractAmount("do sau"))
    @Test fun `teen sau = 300`() = assertEquals(300L, HindiDigits.extractAmount("teen sau"))
    @Test fun `paanch sau = 500`() = assertEquals(500L, HindiDigits.extractAmount("paanch sau"))
    @Test fun `ek hazaar = 1000`() = assertEquals(1000L, HindiDigits.extractAmount("ek hazaar"))
    @Test fun `teen hazaar paanch sau = 3500`() = assertEquals(3500L, HindiDigits.extractAmount("teen hazaar paanch sau"))
    @Test fun `ek lakh = 100000`() = assertEquals(100_000L, HindiDigits.extractAmount("ek lakh"))
    @Test fun `do lakh pachaas hazaar = 250000`() = assertEquals(250_000L, HindiDigits.extractAmount("do lakh pachaas hazaar"))
    @Test fun `arabic 500 = 500`() = assertEquals(500L, HindiDigits.extractAmount("500"))
    @Test fun `arabic 1500 = 1500`() = assertEquals(1500L, HindiDigits.extractAmount("1500"))
    @Test fun `arabic 500 k = 500000`() = assertEquals(500_000L, HindiDigits.extractAmount("500 k"))
    @Test fun `arabic 5 lakh = 500000`() = assertEquals(500_000L, HindiDigits.extractAmount("5 lakh"))
    @Test fun `with rs prefix = 200`() = assertEquals(200L, HindiDigits.extractAmount("Rs 200"))
    @Test fun `full utterance = 200`() = assertEquals(200L, HindiDigits.extractAmount("Ramesh ko do sau ka doodh udhar diya"))
    @Test fun `empty returns null`() = assertNull(HindiDigits.extractAmount(""))
    @Test fun `only text returns null`() = assertNull(HindiDigits.extractAmount("abc"))
    @Test fun `devanagari digits`() = assertEquals(500L, HindiDigits.extractAmount("५००"))
}
