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

class ClockFragment : Fragment() {

    private var _binding: FragmentClockBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    companion object {
        const val ACTIVE_TIME = 5 * 60
        const val REST_TIME = 1 * 60
        const val ACTIVE = 0
        const val REST = 1
        const val THREAD_SLEEP: Long = 10 // this can make the app run faster
        const val PREFS_NAME = "ClockPrefs"
        const val KEY_TIMER_STATE = "timerState"
        const val KEY_CURRENT_TIME = "currentTime"
    }

    private var timerState = ACTIVE
    private var currentTime = ACTIVE_TIME

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

        updateUI()
    }

    private fun startTimer() {
        isRunning = true
        binding.startButton.text = "Stop"
        Thread {
            while (isRunning) {
                while (isRunning && currentTime >= 0) {
                    Thread.sleep(THREAD_SLEEP)
                    if (!isRunning) break
                    currentTime--
                    activity?.runOnUiThread {
                        updateUI()
                    }
                }
                if (isRunning) {
                    switchStateAndUpdateDay()
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
        currentTime = ACTIVE_TIME
        updateUI()
    }

    private fun switchStateAndUpdateDay() {
        if (timerState == ACTIVE) {
            DayAdapter.getInstance()?.addSecStudied(ACTIVE_TIME)
            timerState = REST
            currentTime = REST_TIME
        } else {
            timerState = ACTIVE
            currentTime = ACTIVE_TIME
        }
    }

    private fun updateUI() {
        val minutes = currentTime / 60
        val seconds = currentTime % 60
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds)

        val totalTime = if (timerState == ACTIVE) ACTIVE_TIME else REST_TIME
        val progress = ((totalTime - currentTime).toFloat() / totalTime * 100).toInt()
        binding.progressBar.progress = progress
    }

    private fun loadPreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        timerState = prefs.getInt(KEY_TIMER_STATE, ACTIVE)
        currentTime = prefs.getInt(KEY_CURRENT_TIME, ACTIVE_TIME)
    }

    private fun savePreferences() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(KEY_TIMER_STATE, timerState)
            putInt(KEY_CURRENT_TIME, currentTime)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        savePreferences()
    }
}
