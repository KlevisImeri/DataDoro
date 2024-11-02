package hu.bme.aut.t4xgko.DataDoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import hu.bme.aut.t4xgko.DataDoro.databinding.DialogSettingsBinding

class SettingsDialogFragment : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!

    private var onSaveSettingsListener: OnSaveSettingsListener? = null

    interface OnSaveSettingsListener {
        fun onSaveSettings(studyTime: Int, restTime: Int, threadSleep: Long)
    }

    fun setOnSaveSettingsListener(listener: OnSaveSettingsListener) {
        onSaveSettingsListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRadioGroup()

        binding.btnPickStudyTime.setOnClickListener {
            showTimePicker { hours, minutes ->
                val studyTimeInSeconds = hours * 3600 + minutes * 60
                binding.btnPickStudyTime.text = String.format("%02d:%02d", hours, minutes)
                binding.btnPickStudyTime.tag = studyTimeInSeconds
            }
        }

        binding.btnPickRestTime.setOnClickListener {
            showTimePicker { hours, minutes ->
                val restTimeInSeconds = hours * 3600 + minutes * 60
                binding.btnPickRestTime.text = String.format("%02d:%02d", hours, minutes)
                binding.btnPickRestTime.tag = restTimeInSeconds
            }
        }
    }

    private fun saveSettings() {
        val studyTime = binding.btnPickStudyTime.tag as? Int ?: ClockFragment.STUDY_TIME
        val restTime = binding.btnPickRestTime.tag as? Int ?: ClockFragment.REST_TIME
        val selectedRadioButtonId = binding.radioGroupThreadSleep.checkedRadioButtonId
        val threadSleep = when (selectedRadioButtonId) {
            binding.radioSleep10.id -> 10L
            binding.radioSleep100.id -> 100L
            binding.radioSleep1000.id -> 1000L
            else -> ClockFragment.THREAD_SLEEP
        }

        if (settingsChanged(studyTime, restTime, threadSleep)) {
            onSaveSettingsListener?.onSaveSettings(studyTime, restTime, threadSleep)
        }
    }

    private fun settingsChanged(studyTime: Int, restTime: Int, threadSleep: Long): Boolean {
        return studyTime != ClockFragment.STUDY_TIME ||
               restTime != ClockFragment.REST_TIME ||
               threadSleep != ClockFragment.THREAD_SLEEP
    }

    private fun showTimePicker(onTimeSelected: (hours: Int, minutes: Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSelected(picker.hour, picker.minute)
        }

        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun setupRadioGroup() {
        when (ClockFragment.THREAD_SLEEP) {
            10L -> binding.radioGroupThreadSleep.check(binding.radioSleep10.id)
            100L -> binding.radioGroupThreadSleep.check(binding.radioSleep100.id)
            1000L -> binding.radioGroupThreadSleep.check(binding.radioSleep1000.id)
            else -> binding.radioGroupThreadSleep.clearCheck()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveSettings()
        _binding = null
    }
}
