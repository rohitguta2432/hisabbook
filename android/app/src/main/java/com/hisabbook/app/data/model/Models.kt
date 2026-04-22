package com.hisabbook.app.data.model

import java.util.UUID

enum class EntryType { BIKRI, KHARCH, UDHAR_DIYA, UDHAR_WAPAS, UDHAR_LIYA, UDHAR_CHUKAYA }
enum class PersonType { CUSTOMER, SUPPLIER }
enum class Source { VOICE_LLM, VOICE_VOSK, MANUAL }

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String? = null,
    val type: PersonType = PersonType.CUSTOMER,
    val balancePaise: Long = 0L
)

data class Entry(
    val id: String = UUID.randomUUID().toString(),
    val type: EntryType,
    val personId: String? = null,
    val personName: String? = null,
    val amountPaise: Long,
    val item: String = "",
    val note: String? = null,
    val langCode: String = "hi",
    val createdAt: Long = System.currentTimeMillis(),
    val source: Source = Source.MANUAL
)

