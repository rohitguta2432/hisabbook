package com.hisabbook.app.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hisabbook.app.MainActivity
import com.hisabbook.app.R
import com.hisabbook.app.data.repo.HisabBookRepository
import com.hisabbook.app.util.toRupeesString
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

@HiltWorker
class NudgeWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: HisabBookRepository
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val totals = repo.observeTodayTotals().first()
        val baki = repo.observeTotalBakiUdhar().first()

        val line1 = "Aaj ka munafa ${totals.munafaPaise.toRupeesString()}"
        val line2 = if (baki > 0) "Baki udhar ${baki.toRupeesString()}" else null

        ensureChannel(applicationContext)
        if (!canPostNotifications(applicationContext)) return Result.success()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(line1)
            .setContentText(line2 ?: "HisabBook dekho")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIF_ID, notif)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "hisabbook_daily_nudge"
        private const val NOTIF_ID = 2601
        const val WORK_NAME = "nudge_9pm"

        fun schedule(ctx: Context) {
            val delay = nextNineThirtyDelayMs()
            val request = PeriodicWorkRequestBuilder<NudgeWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun nextNineThirtyDelayMs(): Long {
            val now = Calendar.getInstance()
            val nine = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.DATE, 1)
            }
            return (nine.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
        }

        fun ensureChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Daily Hisab Nudge",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    nm.createNotificationChannel(channel)
                }
            }
        }

        private fun canPostNotifications(ctx: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
}
