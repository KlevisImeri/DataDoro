// ClockService.kt
package hu.bme.aut.t4xgko.DataDoro.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import hu.bme.aut.t4xgko.DataDoro.MainActivity
import hu.bme.aut.t4xgko.DataDoro.R
import kotlinx.coroutines.*

class ClockService : Service() {
    private var isRunning = false
    private var currentTime = 0
    private var timerState = ACTIVE
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        const val CHANNEL_ID = "ClockServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTIVE = 0
        const val REST = 1
        
        private var instance: ClockService? = null
        fun getInstance() = instance

        // Callback interface for updating UI
        interface ClockCallback {
            fun onTimeUpdate(time: Int, state: Int)
            fun onStateChange(state: Int)
        }
        private var callback: ClockCallback? = null
        fun setCallback(cb: ClockCallback?) {
            callback = cb
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        initializeWakeLock()
        initializeMediaPlayer()
    }

    private fun initializeWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DataDoro::ClockWakeLock"
        )
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound)
        mediaPlayer?.isLooping = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        wakeLock?.acquire(24*60*60*1000L /*24 hours*/)
        return START_STICKY
    }

    fun startTimer(initialTime: Int, initialState: Int) {
        currentTime = initialTime
        timerState = initialState
        isRunning = true
        
        serviceScope.launch {
            while (isRunning) {
                while (isRunning && currentTime >= 0) {
                    delay(1000)
                    if (!isRunning) break
                    currentTime--
                    updateNotification()
                    callback?.onTimeUpdate(currentTime, timerState)
                }
                if (isRunning) {
                    playNotificationSound()
                    switchState()
                }
            }
        }
    }

    private fun switchState() {
        if (timerState == ACTIVE) {
            timerState = REST
            currentTime = ClockFragment.REST_TIME
            playNotificationSound()
        } else {
            timerState = ACTIVE
            currentTime = ClockFragment.STUDY_TIME
            playNotificationSound()
        }
        callback?.onStateChange(timerState)
        updateNotification()
    }

    private fun playNotificationSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.prepare()
            }
            it.start()
        }
    }

    fun stopTimer() {
        isRunning = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Clock Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DataDoro Timer")
            .setContentText(formatTime(currentTime))
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(time: Int): String {
        val hours = (time % (24 * 3600)) / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceScope.cancel()
        instance = null
    }
}