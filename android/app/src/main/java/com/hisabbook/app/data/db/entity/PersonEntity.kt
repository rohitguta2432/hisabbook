package com.hisabbook.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType

@Entity(
    tableName = "persons",
    indices = [Index("name"), Index("phone")]
)
data class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String?,
    val type: String,             // stored as PersonType.name
    val balancePaise: Long,
    val createdAt: Long
) {
    fun toDomain() = Person(
        id = id,
        name = name,
        phone = phone,
        type = runCatching { PersonType.valueOf(type) }.getOrDefault(PersonType.CUSTOMER),
        balancePaise = balancePaise
    )

    companion object {
        fun fromDomain(p: Person) = PersonEntity(
            id = p.id,
            name = p.name,
            phone = p.phone,
            type = p.type.name,
            balancePaise = p.balancePaise,
            createdAt = System.currentTimeMillis()
        )
    }
}
