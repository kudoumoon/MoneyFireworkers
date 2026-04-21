package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.HomeDashboardSnapshot
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeDashboard(): Flow<HomeDashboardSnapshot>

    suspend fun refreshDashboard()
}
