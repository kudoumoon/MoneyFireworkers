package com.moneyfireworkers.paytrack.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyfireworkers.paytrack.domain.repository.HomeRepository
import com.moneyfireworkers.paytrack.domain.usecase.home.ObserveHomeDashboardUseCase
import com.moneyfireworkers.paytrack.domain.usecase.home.RefreshHomeDashboardUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val observeHomeDashboardUseCase: ObserveHomeDashboardUseCase,
    private val refreshHomeDashboardUseCase: RefreshHomeDashboardUseCase,
    private val uiStateMapper: HomeUiStateMapper = HomeUiStateMapper(),
) : ViewModel() {
    constructor(homeRepository: HomeRepository) : this(
        observeHomeDashboardUseCase = ObserveHomeDashboardUseCase(homeRepository),
        refreshHomeDashboardUseCase = RefreshHomeDashboardUseCase(homeRepository),
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<HomeUiEvent> = _events.asSharedFlow()

    init {
        observeDashboard()
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.Refresh -> refresh()
            HomeUiAction.AddPaymentClicked -> emitEvent(HomeUiEvent.NavigateToAddPayment)
            HomeUiAction.ViewAllClicked -> emitEvent(HomeUiEvent.NavigateToRecords)
            HomeUiAction.PendingClicked -> handlePendingClick()
            is HomeUiAction.RecentSpendingClicked -> {
                emitEvent(HomeUiEvent.NavigateToEntryDetail(action.entryId))
            }
        }
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            observeHomeDashboardUseCase()
                .catch { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isRefreshing = false,
                            refreshErrorMessage = throwable.message ?: DEFAULT_REFRESH_ERROR_MESSAGE,
                            screenState = if (
                                current.recentSpendings.isEmpty() &&
                                !current.pendingSection.hasPending
                            ) {
                                HomeScreenState.EMPTY
                            } else {
                                current.screenState
                            },
                        )
                    }
                }
                .collectLatest { snapshot ->
                    _uiState.update {
                        uiStateMapper.map(snapshot).copy(
                            isRefreshing = false,
                            refreshErrorMessage = null,
                        )
                    }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isRefreshing = true,
                    refreshErrorMessage = null,
                )
            }
            try {
                refreshHomeDashboardUseCase()
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(
                        refreshErrorMessage = throwable.message ?: DEFAULT_REFRESH_ERROR_MESSAGE,
                    )
                }
            } finally {
                _uiState.update { current ->
                    current.copy(isRefreshing = false)
                }
            }
        }
    }

    private fun handlePendingClick() {
        val latestPending = _uiState.value.pendingSection.latestPending ?: return
        emitEvent(
            HomeUiEvent.NavigateToPending(
                pendingActionId = latestPending.pendingActionId,
                ledgerEntryId = latestPending.ledgerEntryId,
            ),
        )
    }

    private fun emitEvent(event: HomeUiEvent) {
        _events.tryEmit(event)
    }

    private companion object {
        const val DEFAULT_REFRESH_ERROR_MESSAGE = "刷新失败，请稍后再试。"
    }
}
