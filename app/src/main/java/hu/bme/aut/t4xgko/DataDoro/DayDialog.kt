package hu.bme.aut.t4xgko.DataDoro

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.dayrecyclerviewdemo.data.Day
import hu.bme.aut.dayrecyclerviewdemo.databinding.DayDialogBinding
import java.util.Date

class DayDialog : DialogFragment() {

  interface DayHandler {
    fun dayCreated(day: Day)
    fun dayUpdated(day: Day)
  }

  lateinit var dayHandler: DayHandler
  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is DayHandler) {
      dayHandler = context
    } else {
      throw RuntimeException("The Activity is not implementing the DayHandler interface.")
    }
  }

  lateinit var etDayText: EditText
  lateinit var cbDayDone: CheckBox
  lateinit var spinnerCategory: Spinner
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialogBuilder = AlertDialog.Builder(requireContext())
    dialogBuilder.setTitle("Day dialog")
    val dialogBinding = DayDialogBinding.inflate(layoutInflater)
    etDayText = dialogBinding.etDayText
    cbDayDone = dialogBinding.cbDayDone
    spinnerCategory = dialogBinding.spinnerCategory
    var categoryAdapter =
            ArrayAdapter.createFromResource(
                    requireContext()!!,
                    R.array.categories,
                    android.R.layout.simple_spinner_item
            )
    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinnerCategory.adapter = categoryAdapter
    // spinnerCategory.setSelection(1)
    dialogBuilder.setView(dialogBinding.root)

    val arguments = this.arguments
    // if we are in EDIT mode
    if (arguments != null && arguments.containsKey(MainActivity.KEY_EDIT)) {
      val dayItem = arguments.getSerializable(MainActivity.KEY_EDIT) as Day
      etDayText.setText(dayItem.dayText)
      cbDayDone.isChecked = dayItem.done
      dialogBuilder.setTitle("Edit day")
    }

    dialogBuilder.setPositiveButton("Ok") { dialog, which -> }
    dialogBuilder.setNegativeButton("Cancel") { dialog, which -> }
    return dialogBuilder.create()
  }

  override fun onResume() {
    super.onResume()
    val positiveButton = (dialog as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)
    positiveButton.setOnClickListener {
      if (etDayText.text.isNotEmpty()) {
        val arguments = this.arguments
        // IF EDIT MODE
        if (arguments != null && arguments.containsKey(MainActivity.KEY_EDIT)) {
          handleDayEdit()
        } else {
          handleDayCreate()
        }
        dialog!!.dismiss()
      } else {
        etDayText.error = "This field can not be empty"
      }
    }
  }

  private fun handleDayCreate() {
    dayHandler.dayCreated(
            Day(
                    null,
                    Date(System.currentTimeMillis()).toString(),
                    false,
                    etDayText.text.toString(),
                    spinnerCategory.selectedItemPosition
            )
    )
  }

  private fun handleDayEdit() {
    val dayToEdit = arguments?.getSerializable(MainActivity.KEY_EDIT) as Day
    dayToEdit.dayText = etDayText.text.toString()
    dayToEdit.done = cbDayDone.isChecked
    dayHandler.dayUpdated(dayToEdit)
  }
}
