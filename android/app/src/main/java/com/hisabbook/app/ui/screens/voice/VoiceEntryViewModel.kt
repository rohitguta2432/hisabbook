package com.hisabbook.app.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.data.model.Source
import com.hisabbook.app.data.repo.HisabBookRepository
import com.hisabbook.app.domain.parser.ParsedEntry
import com.hisabbook.app.domain.pipeline.VoicePipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VoiceUiState(
    val state: VoiceState = VoiceState.Listening,
    val transcript: String = "",
    val parsed: ParsedEntry? = null
)

@HiltViewModel
class VoiceEntryViewModel @Inject constructor(
    private val pipeline: VoicePipeline,
    private val repo: HisabBookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceUiState())
    val state = _state.asStateFlow()

    fun hasMicPermission() = pipeline.hasMicPermission()

    fun startListening() {
        _state.value = VoiceUiState(state = VoiceState.Listening)
        viewModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(state = VoiceState.Processing)
                val result = pipeline.recordAndParse()
                _state.value = VoiceUiState(
                    state = if (result.parsed.confidence >= 0.5f) VoiceState.Confirm else VoiceState.Error,
                    transcript = result.transcript,
                    parsed = result.parsed
                )
            }.onFailure {
                _state.value = _state.value.copy(state = VoiceState.Error)
            }
        }
    }

    fun confirm() {
        val p = _state.value.parsed ?: return
        val type = p.type ?: return
        val amount = p.amountPaise ?: return
        viewModelScope.launch {
            val personId = p.person?.let { name ->
                val existing = repo.findPersonByName(name)
                if (existing != null) existing.id
                else {
                    val newId = UUID.randomUUID().toString()
                    val inferredType = if (type == EntryType.UDHAR_LIYA || type == EntryType.UDHAR_CHUKAYA)
                        PersonType.SUPPLIER else PersonType.CUSTOMER
                    repo.upsertPerson(
                        Person(
                            id = newId,
                            name = name,
                            phone = null,
                            type = inferredType,
                            balancePaise = 0L
                        )
                    )
                    newId
                }
            }
            repo.saveEntry(
                Entry(
                    type = type,
                    personId = personId,
                    personName = p.person,
                    amountPaise = amount,
                    item = p.item.orEmpty(),
                    note = p.note,
                    source = Source.VOICE_VOSK
                )
            )
        }
    }
}
