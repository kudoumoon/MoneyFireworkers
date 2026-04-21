package com.moneyfireworkers.paytrack.domain.usecase.home

import com.moneyfireworkers.paytrack.domain.repository.HomeRepository

class RefreshHomeDashboardUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke() {
        homeRepository.refreshDashboard()
    }
}
