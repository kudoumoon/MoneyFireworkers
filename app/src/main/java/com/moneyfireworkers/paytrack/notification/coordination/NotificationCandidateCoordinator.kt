package com.moneyfireworkers.paytrack.notification.coordination

import com.moneyfireworkers.paytrack.domain.model.NotificationCandidateCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationCandidateCoordinator {
    private val _currentCandidate = MutableStateFlow<NotificationCandidateCard?>(null)
    val currentCandidate: StateFlow<NotificationCandidateCard?> = _currentCandidate.asStateFlow()

    fun present(candidate: NotificationCandidateCard) {
        _currentCandidate.value = candidate
    }

    fun clear() {
        _currentCandidate.value = null
    }
}
