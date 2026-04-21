package com.moneyfireworkers.paytrack.app

import android.app.Application
import kotlinx.coroutines.runBlocking

class PayTrackApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        runBlocking {
            appContainer.seedInitialData()
        }
    }
}
