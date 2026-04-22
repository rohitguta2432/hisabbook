package com.hisabbook.app.domain.parser

import com.hisabbook.app.data.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerbMapTest {

    @Test fun `udhar diya maps to UDHAR_DIYA`() =
        assertEquals(EntryType.UDHAR_DIYA, VerbMap.detectType("Ramesh ko udhar diya"))

    @Test fun `paisa wapas maps to UDHAR_WAPAS`() =
        assertEquals(EntryType.UDHAR_WAPAS, VerbMap.detectType("Sunil se paisa wapas aaya"))

    @Test fun `jama kiya maps to UDHAR_WAPAS`() =
        assertEquals(EntryType.UDHAR_WAPAS, VerbMap.detectType("jama kiya Ramesh ne"))

    @Test fun `udhar liya maps to UDHAR_LIYA`() =
        assertEquals(EntryType.UDHAR_LIYA, VerbMap.detectType("sabzi wala se udhar liya"))

    @Test fun `chuka diya maps to UDHAR_CHUKAYA`() =
        assertEquals(EntryType.UDHAR_CHUKAYA, VerbMap.detectType("supplier ko chuka diya"))

    @Test fun `bikri maps to BIKRI`() =
        assertEquals(EntryType.BIKRI, VerbMap.detectType("aaj ki bikri 500"))

    @Test fun `kharch maps to KHARCH`() =
        assertEquals(EntryType.KHARCH, VerbMap.detectType("bijli ka kharch"))

    @Test fun `bijli keyword alone maps to KHARCH`() =
        assertEquals(EntryType.KHARCH, VerbMap.detectType("bijli 200"))

    @Test fun `empty string returns null`() =
        assertNull(VerbMap.detectType(""))
}
