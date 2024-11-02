  package hu.bme.aut.t4xgko.DataDoro

  import android.content.Context
  import android.os.Bundle
  import android.os.Handler
  import android.os.Looper
  import android.view.LayoutInflater
  import android.view.View
  import android.view.ViewGroup
  import androidx.fragment.app.Fragment
  import hu.bme.aut.t4xgko.DataDoro.databinding.FragmentClockBinding
  import hu.bme.aut.t4xgko.DataDoro.adapter.DayAdapter
  import hu.bme.aut.t4xgko.DataDoro.SettingsDialogFragment

  class ClockFragment : Fragment() {

      private var _binding: FragmentClockBinding? = null
      private val binding get() = _binding!!

      private val handler = Handler(Looper.getMainLooper())
      private var isRunning = false

      companion object {
          var STUDY_TIME = 20 * 60
          var REST_TIME = 5 * 60
          var THREAD_SLEEP: Long = 1000 // this can make the app run faster
          const val ACTIVE = 0
          const val REST = 1
          const val PREFS_NAME = "ClockPrefs"
          const val KEY_TIMER_STATE = "timerState"
          const val KEY_CURRENT_TIME = "currentTime"
          const val KEY_STUDY_TIME = "studyTime"
          const val KEY_REST_TIME = "restTime"
          const val KEY_THREAD_SLEEP = "threadSleep"
      }

      private var timerState = ACTIVE
      private var currentTime = STUDY_TIME

      override fun onCreateView(
          inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?
      ): View? {
          _binding = FragmentClockBinding.inflate(inflater, container, false)
          return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)

          loadPreferences()
          updateUI()

          binding.startButton.setOnClickListener {
              if (isRunning) {
                  stopTimer()
              } else {
                  startTimer()
              }
          }

          binding.resetButton.setOnClickListener {
              resetTimer()
          }

          binding.timerText.setOnClickListener {
              showSettingsDialog()
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

      var totalToUpdateDay = 0;
      private fun startTimer() {
          isRunning = true
          binding.startButton.text = "Stop"
          Thread {
              while (isRunning) {
                  while (isRunning && currentTime >= 0) {
                      Thread.sleep(THREAD_SLEEP)
                      if (!isRunning) break
                      currentTime--
                      if(timerState == ACTIVE) totalToUpdateDay++
                      activity?.runOnUiThread {
                          updateUI()
                          if (timerState == ACTIVE && currentTime % 60 == 0) {  // Save every 60 sec
                              updateDay() 
                          }
                          if ( currentTime % 10 == 0) {
                             savePreferences()
                          }
                      }
                  }
                  if (isRunning) {
                      switchState()
                      activity?.runOnUiThread {
                          updateUI()
                      }
                  }
              }
          }.start()
      }

      private fun stopTimer() {
          isRunning = false
          binding.startButton.text = "Start"
      }

      private fun resetTimer() {
          stopTimer()
          timerState = ACTIVE
          currentTime = STUDY_TIME
          updateUI()
      }

      private fun switchState() {
          if (timerState == ACTIVE) {
              timerState = REST
              currentTime = REST_TIME
          } else {
              timerState = ACTIVE
              currentTime = STUDY_TIME
          }
      }

      private fun updateDay() {
          DayAdapter.getInstance()?.addSecStudied(totalToUpdateDay)
          totalToUpdateDay=0
      }

      private fun updateUI() {
          val hours = (currentTime % (24 * 3600)) / 3600
          val minutes = (currentTime % 3600) / 60
          val seconds = currentTime % 60

          binding.timerText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

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

      override fun onDestroyView() {
          _binding = null
          super.onDestroyView()
      }
  }
