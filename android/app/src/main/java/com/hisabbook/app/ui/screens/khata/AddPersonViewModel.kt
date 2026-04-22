package com.hisabbook.app.ui.screens.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.data.repo.HisabBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val repo: HisabBookRepository
) : ViewModel() {
    fun save(name: String, phone: String?, type: PersonType) {
        viewModelScope.launch {
            repo.upsertPerson(
                Person(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    phone = phone,
                    type = type,
                    balancePaise = 0L
                )
            )
        }
    }
}
