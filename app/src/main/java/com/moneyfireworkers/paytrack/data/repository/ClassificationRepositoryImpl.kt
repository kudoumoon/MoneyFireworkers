package com.moneyfireworkers.paytrack.data.repository

import com.moneyfireworkers.paytrack.data.local.dao.ClassificationRuleDao
import com.moneyfireworkers.paytrack.data.local.mapper.ClassificationRuleMapper
import com.moneyfireworkers.paytrack.domain.model.ClassificationRule
import com.moneyfireworkers.paytrack.domain.repository.ClassificationRepository

class ClassificationRepositoryImpl(
    private val classificationRuleDao: ClassificationRuleDao,
) : ClassificationRepository {
    override suspend fun getAllEnabledRules(): List<ClassificationRule> {
        return classificationRuleDao.getAllEnabled().map(ClassificationRuleMapper::fromEntity)
    }
}
