package com.hisabbook.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Source

@Entity(
    tableName = "entries",
    indices = [Index("personId"), Index("createdAt"), Index("type")],
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class EntryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val personId: String?,
    val personName: String?,
    val amountPaise: Long,
    val item: String,
    val note: String?,
    val langCode: String,
    val createdAt: Long,
    val source: String
) {
    fun toDomain() = Entry(
        id = id,
        type = runCatching { EntryType.valueOf(type) }.getOrDefault(EntryType.KHARCH),
        personId = personId,
        personName = personName,
        amountPaise = amountPaise,
        item = item,
        note = note,
        langCode = langCode,
        createdAt = createdAt,
        source = runCatching { Source.valueOf(source) }.getOrDefault(Source.MANUAL)
    )

    companion object {
        fun fromDomain(e: Entry) = EntryEntity(
            id = e.id,
            type = e.type.name,
            personId = e.personId,
            personName = e.personName,
            amountPaise = e.amountPaise,
            item = e.item,
            note = e.note,
            langCode = e.langCode,
            createdAt = e.createdAt,
            source = e.source.name
        )
    }
}
