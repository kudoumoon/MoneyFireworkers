package com.moneyfireworkers.paytrack.domain.usecase.home

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import com.moneyfireworkers.paytrack.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class ObserveHomeDashboardUseCase(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(): Flow<HomeDashboardSnapshot> {
        return homeRepository.observeDashboard()
    }
}
