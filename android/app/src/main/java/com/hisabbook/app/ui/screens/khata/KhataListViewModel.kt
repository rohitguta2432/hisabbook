package com.hisabbook.app.ui.screens.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.data.repo.HisabBookRepository
import com.hisabbook.app.data.repo.SeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class KhataListViewModel @Inject constructor(
    private val repo: HisabBookRepository,
    private val seed: SeedData
) : ViewModel() {

    private val _typeFilter = MutableStateFlow(PersonType.CUSTOMER)
    val typeFilter: StateFlow<PersonType> = _typeFilter

    init {
        viewModelScope.launch { seed.seedIfNeeded() }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val persons: StateFlow<List<Person>> =
        _typeFilter.flatMapLatest { type -> repo.observePersons(type) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectType(type: PersonType) { _typeFilter.value = type }

    fun addPerson(p: Person) {
        viewModelScope.launch { repo.upsertPerson(p) }
    }
}
