package com.hisabbook.app.ui.screens.khata

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.Source
import com.hisabbook.app.data.repo.HisabBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerKhataViewModel @Inject constructor(
    private val repo: HisabBookRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val personIdFlow = MutableStateFlow(savedState.get<String>("personId") ?: "")

    fun setPersonId(id: String) { personIdFlow.value = id }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val person: StateFlow<Person?> = personIdFlow.flatMapLatest { id ->
        kotlinx.coroutines.flow.flow {
            emit(if (id.isBlank()) null else repo.getPerson(id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<Entry>> = personIdFlow.flatMapLatest { id ->
        if (id.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList())
        else repo.observeEntriesForPerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEntry(entry: Entry) { viewModelScope.launch { repo.deleteEntry(entry) } }

    fun settle(entry: Entry) {
        if (entry.type == EntryType.UDHAR_DIYA) {
            val settle = Entry(
                type = EntryType.UDHAR_WAPAS,
                personId = entry.personId,
                personName = entry.personName,
                amountPaise = entry.amountPaise,
                item = "Jama Kiya",
                note = "Settled: ${entry.item}",
                source = Source.MANUAL
            )
            viewModelScope.launch { repo.saveEntry(settle) }
        } else if (entry.type == EntryType.UDHAR_LIYA) {
            val settle = Entry(
                type = EntryType.UDHAR_CHUKAYA,
                personId = entry.personId,
                personName = entry.personName,
                amountPaise = entry.amountPaise,
                item = "Chuka Diya",
                note = "Settled: ${entry.item}",
                source = Source.MANUAL
            )
            viewModelScope.launch { repo.saveEntry(settle) }
        }
    }
}
