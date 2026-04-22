package com.hisabbook.app.data.repo

import com.hisabbook.app.data.mock.MockData
import com.hisabbook.app.data.prefs.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class SeedData @Inject constructor(
    private val repo: HisabBookRepository,
    private val prefs: AppPreferences
) {
    suspend fun seedIfNeeded() {
        val existing = repo.observePersons().first()
        if (existing.isNotEmpty()) return
        MockData.persons.forEach { repo.upsertPerson(it) }
        MockData.entries.forEach { repo.saveEntry(it) }
    }
}
