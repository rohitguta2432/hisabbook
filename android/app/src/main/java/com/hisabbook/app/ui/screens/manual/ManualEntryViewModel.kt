package com.hisabbook.app.ui.screens.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.data.model.Source
import com.hisabbook.app.data.repo.HisabBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val repo: HisabBookRepository
) : ViewModel() {

    val persons: StateFlow<List<Person>> = repo.observePersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(
        type: EntryType,
        amountPaise: Long,
        personName: String?,
        item: String,
        note: String?
    ) {
        viewModelScope.launch {
            val personId = personName?.takeIf { it.isNotBlank() }?.let { name ->
                val existing = persons.value.firstOrNull { it.name.equals(name, ignoreCase = true) }
                if (existing != null) existing.id
                else {
                    val inferredType = when (type) {
                        EntryType.UDHAR_LIYA, EntryType.UDHAR_CHUKAYA -> PersonType.SUPPLIER
                        else -> PersonType.CUSTOMER
                    }
                    val p = Person(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        phone = null,
                        type = inferredType,
                        balancePaise = 0L
                    )
                    repo.upsertPerson(p)
                    p.id
                }
            }
            repo.saveEntry(
                Entry(
                    type = type,
                    personId = personId,
                    personName = personName,
                    amountPaise = amountPaise,
                    item = item,
                    note = note,
                    source = Source.MANUAL
                )
            )
        }
    }
}
