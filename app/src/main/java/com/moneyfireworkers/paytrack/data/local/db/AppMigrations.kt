package com.moneyfireworkers.paytrack.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE ledger_entries ADD COLUMN emotion TEXT DEFAULT NULL",
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification_recognition_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    packageName TEXT NOT NULL,
                    title TEXT,
                    contentText TEXT NOT NULL,
                    amountInCent INTEGER,
                    merchantName TEXT,
                    occurredAt INTEGER,
                    recognitionStatus TEXT NOT NULL,
                    paymentEventId INTEGER,
                    ledgerEntryId INTEGER,
                    pendingActionId INTEGER,
                    rawPayload TEXT NOT NULL,
                    failureReason TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    const val CURRENT_VERSION = 3
}
