package hu.bme.aut.t4xgko.DataDoro.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.t4xgko.DataDoro.DayActivity
import hu.bme.aut.t4xgko.DataDoro.MainActivity
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.databinding.DayRowBinding
import hu.bme.aut.t4xgko.DataDoro.touch.DayTouchHelperCallback
import java.util.Collections

class DayAdapter : RecyclerView.Adapter<DayAdapter.ViewHolder>, DayTouchHelperCallback {

  inner class ViewHolder(val binding: DayRowBinding) : RecyclerView.ViewHolder(binding.root) {}

  var dayItems = mutableListOf<Day>()
  val context: Context

  constructor(context: Context, listDays: List<Day>) {
    this.context = context
    dayItems.addAll(listDays)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(DayRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
  }

  override fun getItemCount(): Int {
    return dayItems.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    var thisDay = dayItems[position]
    holder.binding.tvDate.text = dayItems[position].YearMonthDay

    holder.itemView.setOnClickListener {
      val dayId = thisDay.YearMonthDay
    
      val intent = Intent(holder.itemView.context, DayActivity::class.java).apply {
          putExtra("DAY_ID", dayId)
      }
      context.startActivity(intent)
    }
  }

  private fun deleteDay(position: Int) {
    Thread {
              AppDatabase.getInstance(context).DayDao().deleteDay(dayItems.get(position))
              (context as MainActivity).runOnUiThread {
                dayItems.removeAt(position)
                notifyItemRemoved(position)
              }
            }
            .start()
  }

  public fun addDay(day: Day) {
    dayItems.add(day)
    // notifyDataSetChanged() // this refreshes the whole list
    notifyItemInserted(dayItems.lastIndex)
  }

  public fun updateDay(day: Day, editIndex: Int) {
    dayItems.set(editIndex, day)
    notifyItemChanged(editIndex)
  }

  override fun onDismissed(position: Int) {
    deleteDay(position)
  }

  override fun onItemMoved(fromPosition: Int, toPosition: Int) {
    Collections.swap(dayItems, fromPosition, toPosition)
    notifyItemMoved(fromPosition, toPosition)
  }
}
