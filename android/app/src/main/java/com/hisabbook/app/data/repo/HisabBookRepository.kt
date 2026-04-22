package com.hisabbook.app.data.repo

import com.hisabbook.app.data.db.HisabBookDb
import com.hisabbook.app.data.db.entity.EntryEntity
import com.hisabbook.app.data.db.entity.PersonEntity
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class HisabBookRepository @Inject constructor(
    private val db: HisabBookDb
) {
    private val personDao get() = db.personDao()
    private val entryDao get() = db.entryDao()

    fun observePersons(type: PersonType? = null): Flow<List<Person>> =
        (if (type == null) personDao.observeAll() else personDao.observeByType(type.name))
            .map { list -> list.map { it.toDomain() } }

    suspend fun getPerson(id: String): Person? = personDao.getById(id)?.toDomain()

    suspend fun findPersonByName(name: String): Person? =
        personDao.observeAll().first()
            .firstOrNull { it.name.equals(name, ignoreCase = true) }?.toDomain()

    suspend fun upsertPerson(p: Person) = personDao.upsert(PersonEntity.fromDomain(p))

    suspend fun deletePerson(id: String) = personDao.delete(id)

    fun observeEntriesForPerson(personId: String): Flow<List<Entry>> =
        entryDao.observeByPerson(personId).map { list -> list.map { it.toDomain() } }

    fun observeAllEntries(): Flow<List<Entry>> =
        entryDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveEntry(entry: Entry) {
        db.runInTransaction {
        }
        entryDao.upsert(EntryEntity.fromDomain(entry))
        entry.personId?.let { pid ->
            val delta = when (entry.type) {
                EntryType.UDHAR_DIYA -> entry.amountPaise
                EntryType.UDHAR_WAPAS -> -entry.amountPaise
                EntryType.UDHAR_LIYA -> -entry.amountPaise
                EntryType.UDHAR_CHUKAYA -> entry.amountPaise
                else -> 0L
            }
            if (delta != 0L) personDao.addToBalance(pid, delta)
        }
    }

    suspend fun deleteEntry(entry: Entry) {
        entryDao.deleteById(entry.id)
        entry.personId?.let { pid ->
            val reverse = when (entry.type) {
                EntryType.UDHAR_DIYA -> -entry.amountPaise
                EntryType.UDHAR_WAPAS -> entry.amountPaise
                EntryType.UDHAR_LIYA -> entry.amountPaise
                EntryType.UDHAR_CHUKAYA -> -entry.amountPaise
                else -> 0L
            }
            if (reverse != 0L) personDao.addToBalance(pid, reverse)
        }
    }

    /** Observe today's totals (bikri, kharch, udhar given, paid-back, munafa). */
    fun observeTodayTotals(): Flow<DailyTotals> {
        val (from, to) = todayRange()
        val bikri = entryDao.sumForTypeBetween(EntryType.BIKRI.name, from, to)
        val kharch = entryDao.sumForTypeBetween(EntryType.KHARCH.name, from, to)
        val udharDiya = entryDao.sumForTypeBetween(EntryType.UDHAR_DIYA.name, from, to)
        val udharWapas = entryDao.sumForTypeBetween(EntryType.UDHAR_WAPAS.name, from, to)
        return combine(bikri, kharch, udharDiya, udharWapas) { b, k, d, w ->
            DailyTotals(
                bikriPaise = b,
                kharchPaise = k,
                udharDiyaPaise = d,
                jamaPaise = w,
                munafaPaise = b - k
            )
        }
    }

    /** Baki-udhar across all customers (sum of positive balances). */
    fun observeTotalBakiUdhar(): Flow<Long> =
        personDao.observeAll().map { list ->
            list.filter { it.balancePaise > 0 }.sumOf { it.balancePaise }
        }

    private fun todayRange(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        val from = c.timeInMillis
        val to = from + 24L * 60 * 60 * 1000 - 1
        return from to to
    }
}

data class DailyTotals(
    val bikriPaise: Long,
    val kharchPaise: Long,
    val udharDiyaPaise: Long,
    val jamaPaise: Long,
    val munafaPaise: Long
)
