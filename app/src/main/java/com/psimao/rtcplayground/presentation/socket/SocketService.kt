package com.psimao.rtcplayground.presentation.socket

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.presentation.call.CallActivity
import com.psimao.rtcplayground.presentation.home.HomeActivity
import org.koin.android.ext.android.inject
import java.util.*


class SocketService : Service(), SignallingView {

    companion object {
        private const val SERVICE_RUNNING_NOTIFICATION_ID = 42
        private val CHANNEL_ID = UUID.randomUUID().toString()
        var isConnected = false
            private set
    }

    private val presenter: SignallingPresenter by inject()

    private var notification: Notification? = null
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        presenter.create(this)
        isConnected = true
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
        isConnected = false
    }

    override fun showNotification() {
        notification = createNotification()
        notificationManager.notify(SERVICE_RUNNING_NOTIFICATION_ID, notification!!)
        startForeground(SERVICE_RUNNING_NOTIFICATION_ID, notification)
    }

    override fun dismissNotification() {
        notificationManager.cancel(SERVICE_RUNNING_NOTIFICATION_ID)
    }

    override fun openCallView(room: String, alias: String) {
        Handler(mainLooper).post {
            startActivity(CallActivity.createIntent(this, room, alias).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    @Suppress("DEPRECATION")
    private fun createNotificationBuilder(): Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Process Running"
        val description = "Show when RTC Playground is running and connected to the web socket."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
        Notification.Builder(this, CHANNEL_ID)
    } else {
        Notification.Builder(this)
    }

    private fun createHomeActivityPendingIntent(): PendingIntent =
            PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, HomeActivity::class.java),
                    0)

    private fun createNotification(): Notification = createNotificationBuilder().setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_vera)
            .setContentIntent(createHomeActivityPendingIntent())
            .setOngoing(true)
            .build()
}