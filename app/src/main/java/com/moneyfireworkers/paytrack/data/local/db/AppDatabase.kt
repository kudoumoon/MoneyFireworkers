package com.moneyfireworkers.paytrack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moneyfireworkers.paytrack.data.local.dao.CategoryDao
import com.moneyfireworkers.paytrack.data.local.dao.ClassificationRuleDao
import com.moneyfireworkers.paytrack.data.local.dao.LedgerEntryDao
import com.moneyfireworkers.paytrack.data.local.dao.NotificationRecognitionLogDao
import com.moneyfireworkers.paytrack.data.local.dao.PaymentEventDao
import com.moneyfireworkers.paytrack.data.local.dao.PendingActionDao
import com.moneyfireworkers.paytrack.data.local.entity.CategoryCorrectionLogEntity
import com.moneyfireworkers.paytrack.data.local.entity.CategoryEntity
import com.moneyfireworkers.paytrack.data.local.entity.ClassificationDecisionLogEntity
import com.moneyfireworkers.paytrack.data.local.entity.ClassificationRuleEntity
import com.moneyfireworkers.paytrack.data.local.entity.LedgerEntryEntity
import com.moneyfireworkers.paytrack.data.local.entity.NotificationRecognitionLogEntity
import com.moneyfireworkers.paytrack.data.local.entity.PaymentEventEntity
import com.moneyfireworkers.paytrack.data.local.entity.PendingActionEntity

@Database(
    entities = [
        PaymentEventEntity::class,
        LedgerEntryEntity::class,
        PendingActionEntity::class,
        CategoryEntity::class,
        ClassificationRuleEntity::class,
        ClassificationDecisionLogEntity::class,
        CategoryCorrectionLogEntity::class,
        NotificationRecognitionLogEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentEventDao(): PaymentEventDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun pendingActionDao(): PendingActionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun classificationRuleDao(): ClassificationRuleDao
    abstract fun notificationRecognitionLogDao(): NotificationRecognitionLogDao
}
