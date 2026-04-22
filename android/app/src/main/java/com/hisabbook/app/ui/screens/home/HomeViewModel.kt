package com.hisabbook.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.repo.DailyTotals
import com.hisabbook.app.data.repo.HisabBookRepository
import com.hisabbook.app.data.repo.SeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val totals: DailyTotals = DailyTotals(0, 0, 0, 0, 0),
    val bakiUdharPaise: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HisabBookRepository,
    private val seed: SeedData
) : ViewModel() {

    init {
        viewModelScope.launch { seed.seedIfNeeded() }
    }

    val state: StateFlow<HomeUiState> = combine(
        repo.observeTodayTotals(),
        repo.observeTotalBakiUdhar()
    ) { t, baki -> HomeUiState(totals = t, bakiUdharPaise = baki) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
