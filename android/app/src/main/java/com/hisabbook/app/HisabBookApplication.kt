package com.hisabbook.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hisabbook.app.worker.NudgeWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class HisabBookApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        NudgeWorker.ensureChannel(this)
        NudgeWorker.schedule(this)
    }
}
