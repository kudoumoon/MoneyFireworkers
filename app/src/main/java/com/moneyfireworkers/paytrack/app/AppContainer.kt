package com.moneyfireworkers.paytrack.app

import android.app.Application
import androidx.room.Room
import com.moneyfireworkers.paytrack.data.local.db.AppDatabase
import com.moneyfireworkers.paytrack.data.local.db.AppMigrations
import com.moneyfireworkers.paytrack.data.local.seed.LocalSeedBootstrapper
import com.moneyfireworkers.paytrack.data.repository.CategoryRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.ClassificationRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.LedgerRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.LocalHomeRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.NotificationRecognitionLogRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.PaymentRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.PendingRepositoryImpl
import com.moneyfireworkers.paytrack.data.repository.SettingsRepositoryImpl
import com.moneyfireworkers.paytrack.domain.usecase.expense.RecordExpenseUseCase
import com.moneyfireworkers.paytrack.domain.usecase.notification.ProcessWeChatNotificationUseCase
import com.moneyfireworkers.paytrack.domain.usecase.pending.ConfirmPendingEntryWithEditUseCase
import com.moneyfireworkers.paytrack.domain.usecase.pending.ConfirmPendingEntryUseCase
import com.moneyfireworkers.paytrack.domain.usecase.pending.IgnorePendingEntryUseCase
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessFormPaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessImagePaymentUseCase
import com.moneyfireworkers.paytrack.domain.usecase.process.ProcessTextPaymentUseCase

class AppContainer(
    application: Application,
) {
    val applicationContext = application.applicationContext

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        DATABASE_NAME,
    )
        .addMigrations(AppMigrations.MIGRATION_1_2)
        .addMigrations(AppMigrations.MIGRATION_2_3)
        .build()

    private val seedBootstrapper = LocalSeedBootstrapper(
        categoryDao = database.categoryDao(),
        classificationRuleDao = database.classificationRuleDao(),
    )

    val paymentRepository = PaymentRepositoryImpl(database.paymentEventDao())
    val ledgerRepository = LedgerRepositoryImpl(database.ledgerEntryDao())
    val pendingRepository = PendingRepositoryImpl(database.pendingActionDao())
    val categoryRepository = CategoryRepositoryImpl(database.categoryDao())
    val classificationRepository = ClassificationRepositoryImpl(database.classificationRuleDao())
    val notificationRecognitionLogRepository = NotificationRecognitionLogRepositoryImpl(
        database.notificationRecognitionLogDao(),
    )
    val settingsRepository = SettingsRepositoryImpl(application)
    val homeRepository = LocalHomeRepositoryImpl(
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        categoryRepository = categoryRepository,
        onRefresh = ::seedInitialData,
    )

    val processTextPaymentUseCase = ProcessTextPaymentUseCase(
        paymentRepository = paymentRepository,
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        classificationRepository = classificationRepository,
    )

    val processFormPaymentUseCase = ProcessFormPaymentUseCase(
        paymentRepository = paymentRepository,
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        classificationRepository = classificationRepository,
    )

    val processImagePaymentUseCase = ProcessImagePaymentUseCase(
        paymentRepository = paymentRepository,
    )

    val confirmPendingEntryUseCase = ConfirmPendingEntryUseCase(
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        paymentRepository = paymentRepository,
    )

    val confirmPendingEntryWithEditUseCase = ConfirmPendingEntryWithEditUseCase(
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        paymentRepository = paymentRepository,
    )

    val ignorePendingEntryUseCase = IgnorePendingEntryUseCase(
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        paymentRepository = paymentRepository,
    )

    val processWeChatNotificationUseCase = ProcessWeChatNotificationUseCase(
        paymentRepository = paymentRepository,
        ledgerRepository = ledgerRepository,
        pendingRepository = pendingRepository,
        classificationRepository = classificationRepository,
        logRepository = notificationRecognitionLogRepository,
    )

    val recordExpenseUseCase = RecordExpenseUseCase(
        paymentRepository = paymentRepository,
        ledgerRepository = ledgerRepository,
        categoryRepository = categoryRepository,
    )

    suspend fun seedInitialData() {
        seedBootstrapper.seedIfNeeded()
    }

    private companion object {
        const val DATABASE_NAME = "paytrack.db"
    }
}
