package com.moneyfireworkers.paytrack.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    onAddPayment: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenPending: (pendingActionId: Long, ledgerEntryId: Long) -> Unit,
    onOpenEntryDetail: (entryId: Long) -> Unit,
    viewModelFactory: HomeViewModelFactory,
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                HomeUiEvent.NavigateToAddPayment -> onAddPayment()
                HomeUiEvent.NavigateToRecords -> onOpenRecords()
                is HomeUiEvent.NavigateToEntryDetail -> onOpenEntryDetail(event.entryId)
                is HomeUiEvent.NavigateToPending -> {
                    onOpenPending(
                        event.pendingActionId,
                        event.ledgerEntryId,
                    )
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}
