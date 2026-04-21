package com.moneyfireworkers.paytrack.domain.repository

import com.moneyfireworkers.paytrack.domain.model.ClassificationRule

interface ClassificationRepository {
    suspend fun getAllEnabledRules(): List<ClassificationRule>
}
