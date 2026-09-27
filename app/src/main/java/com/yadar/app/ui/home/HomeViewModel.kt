package com.yadar.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yadar.app.domain.usecase.GetHomeSummaryUseCase
import com.yadar.app.domain.usecase.HomeSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    getHomeSummaryUseCase: GetHomeSummaryUseCase
) : ViewModel() {

    val summary: StateFlow<HomeSummary?> = getHomeSummaryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
