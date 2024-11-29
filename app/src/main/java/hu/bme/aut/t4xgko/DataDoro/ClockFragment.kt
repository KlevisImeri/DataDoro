package hu.bme.aut.t4xgko.DataDoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import hu.bme.aut.t4xgko.DataDoro.databinding.FragmentClockBinding
import hu.bme.aut.t4xgko.DataDoro.adapter.DayAdapter
import hu.bme.aut.t4xgko.DataDoro.SettingsDialogFragment
import hu.bme.aut.t4xgko.DataDoro.permissionHandlers.NotificationPermissionHandler


class ClockFragment : Fragment() {
    private var _binding: FragmentClockBinding? = null
    private val binding get() = _binding!!

    private var isRunning = false
    private lateinit var notificationPermissionHandler: NotificationPermissionHandler
    private var hasNotificationPermission = false

    companion object {
        var STUDY_TIME = 20 * 60
        var REST_TIME = 5 * 60
        var THREAD_SLEEP: Long = 1000
        const val ACTIVE = 0
        const val REST = 1
        const val PREFS_NAME = "ClockPrefs"
        const val KEY_TIMER_STATE = "timerState"
        const val KEY_CURRENT_TIME = "currentTime"
        const val KEY_STUDY_TIME = "studyTime"
        const val KEY_REST_TIME = "restTime"
        const val KEY_THREAD_SLEEP = "threadSleep"
        private const val NOTIFICATION_CHANNEL_ID = "TIMER_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
    }

    private var timerState = ACTIVE
    private var currentTime = STUDY_TIME
    private var startSound: MediaPlayer? = null
    private var endSound: MediaPlayer? = null
 
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNotificationPermissionHandler()
        createNotificationChannel()
        loadPreferences()
        initializeMediaPlayers()
        updateUI()

        binding.startButton.setOnClickListener {
            if (isRunning) {
                stopTimer()
            } else {
                if (hasNotificationPermission) {
                    startTimer()
                } else {
                    notificationPermissionHandler.checkAndRequestPermissions()
                }
            }
        }

        binding.resetButton.setOnClickListener {
            resetTimer()
        }

        binding.timerText.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun initializeMediaPlayers() {
        context?.let { ctx ->
            startSound = MediaPlayer.create(ctx, R.raw.clock_start)
            endSound = MediaPlayer.create(ctx, R.raw.clock_end)
        }
    }

    private fun setupNotificationPermissionHandler() {
        notificationPermissionHandler = NotificationPermissionHandler(requireActivity() as AppCompatActivity)
        notificationPermissionHandler.setupPermissions {
            hasNotificationPermission = true
            startTimer()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Timer Notification"
            val descriptionText = "Notification for running timer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification() {
        if (!hasNotificationPermission) return

        val stateString = if (timerState == ACTIVE) "Study Time" else "Rest Time"

        val notificationBuilder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.clock)
            .setContentTitle(stateString)
            .setContentText("Time remaining: ${formatTime(currentTime)}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) 
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setVibrate(null);

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun formatTime(timeInSeconds: Int): String {
      val hours = (timeInSeconds % (24 * 3600)) / 3600
      val minutes = (timeInSeconds % 3600) / 60
      val seconds = timeInSeconds % 60
      return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun cancelNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    var totalToUpdateDay = 0
    private fun startTimer() {
      isRunning = true
      binding.startButton.text = "Stop"
      startSound?.start()
      updateNotification()

      val handler = Handler(Looper.getMainLooper())
      val runnable = object : Runnable {
          override fun run() {
              if (!isRunning) return

              if (currentTime >= 0) {
                  currentTime--
                  updateUI()

                  if (timerState == ACTIVE){
                    totalToUpdateDay++
                    if (currentTime % 60 == 0) {
                      updateDay()
                    }
                  } 
                  
                  if (currentTime % 10 == 0) {
                      savePreferences()
                      updateNotification()
                  }

                  handler.postDelayed(this, THREAD_SLEEP)
              } else {
                  if (isRunning) {
                      switchState()
                      updateUI()
                      updateNotification()
                  }
              }
          }
      }

      handler.post(runnable)
    }

    private fun stopTimer() {
        isRunning = false
        binding.startButton.text = "Start"
        cancelNotification()
    }

    private fun resetTimer() {
        stopTimer()
        timerState = ACTIVE
        currentTime = STUDY_TIME
        updateUI()
    }

    private fun switchState() {
        if (timerState == ACTIVE) {
            endSound?.start()
            timerState = REST
            currentTime = REST_TIME
        } else {
            startSound?.start()
            timerState = ACTIVE
            currentTime = STUDY_TIME
        }
    }

    private fun updateDay() {
        DayAdapter.getInstance()?.addSecStudied(totalToUpdateDay)
        totalToUpdateDay = 0
    }

    private fun updateUI() {
        binding.timerText.text = formatTime(currentTime)

        val totalTime = if (timerState == ACTIVE) STUDY_TIME else REST_TIME
        val progress = ((totalTime - currentTime).toFloat() / totalTime * 100).toInt()
        binding.progressBar.progress = progress
    }

    private fun loadPreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        timerState = prefs.getInt(KEY_TIMER_STATE, ACTIVE)
        currentTime = prefs.getInt(KEY_CURRENT_TIME, STUDY_TIME)
        STUDY_TIME = prefs.getInt(KEY_STUDY_TIME, STUDY_TIME)
        REST_TIME = prefs.getInt(KEY_REST_TIME, REST_TIME)
        THREAD_SLEEP = prefs.getLong(KEY_THREAD_SLEEP, THREAD_SLEEP)
    }

    private fun savePreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(KEY_TIMER_STATE, timerState)
            putInt(KEY_CURRENT_TIME, currentTime)
            putInt(KEY_STUDY_TIME, STUDY_TIME)
            putInt(KEY_REST_TIME, REST_TIME)
            putLong(KEY_THREAD_SLEEP, THREAD_SLEEP)
            apply()
        }
    }

    private fun showSettingsDialog() {
        val dialog = SettingsDialogFragment()
        dialog.setOnSaveSettingsListener(object : SettingsDialogFragment.OnSaveSettingsListener {
            override fun onSaveSettings(studyTime: Int, restTime: Int, threadSleep: Long) {
                STUDY_TIME = studyTime
                REST_TIME = restTime
                THREAD_SLEEP = threadSleep
                resetTimer()
                savePreferences()
            }
        })
        dialog.show(parentFragmentManager, "SettingsDialog")
    }

    private fun releaseMediaPlayers() {
        startSound?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        endSound?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        startSound = null
        endSound = null
    }


    override fun onDestroyView() {
        if (isRunning) {
            cancelNotification()
        }
        releaseMediaPlayers()
        _binding = null
        super.onDestroyView()
    }
}