package com.moneyfireworkers.paytrack.data.local.seed

import com.moneyfireworkers.paytrack.data.local.dao.CategoryDao
import com.moneyfireworkers.paytrack.data.local.dao.ClassificationRuleDao

class LocalSeedBootstrapper(
    private val categoryDao: CategoryDao,
    private val classificationRuleDao: ClassificationRuleDao,
) {
    suspend fun seedIfNeeded(now: Long = System.currentTimeMillis()) {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(DefaultCategoriesSeed.build(now))
        }
        if (classificationRuleDao.count() == 0) {
            classificationRuleDao.insertAll(DefaultRulesSeed.build(now))
        }
    }
}
