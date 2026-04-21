package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import com.moneyfireworkers.paytrack.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHomeRepositoryImpl(
    private var scenario: HomeFakeScenario = HomeFakeScenario.POPULATED,
    private val provider: HomeFakeDashboardProvider = HomeFakeDashboardProvider(),
) : HomeRepository {
    private val dashboardState = MutableStateFlow(provider.createSnapshot(scenario))

    override fun observeDashboard(): Flow<HomeDashboardSnapshot> {
        return dashboardState.asStateFlow()
    }

    override suspend fun refreshDashboard() {
        dashboardState.value = provider.createSnapshot(scenario)
    }

    fun setScenario(scenario: HomeFakeScenario) {
        this.scenario = scenario
        dashboardState.value = provider.createSnapshot(scenario)
    }
}
