package com.hisabbook.app.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.repo.DailyTotals
import com.hisabbook.app.data.repo.HisabBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SummaryViewModel @Inject constructor(
    repo: HisabBookRepository
) : ViewModel() {
    val totals: StateFlow<DailyTotals> = repo.observeTodayTotals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyTotals(0, 0, 0, 0, 0))
}
