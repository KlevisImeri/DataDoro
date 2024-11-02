package hu.bme.aut.t4xgko.DataDoro.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.t4xgko.DataDoro.DayActivity
import hu.bme.aut.t4xgko.DataDoro.MainActivity
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.databinding.DayRowBinding
import hu.bme.aut.t4xgko.DataDoro.touch.DayTouchHelperCallback
import java.util.Collections
import java.time.LocalDate

class DayAdapter private constructor(private val context: Context) : RecyclerView.Adapter<DayAdapter.ViewHolder>(), DayTouchHelperCallback {

    inner class ViewHolder(val binding: DayRowBinding) : RecyclerView.ViewHolder(binding.root) {}

    var dayItems = mutableListOf<Day>()

    companion object {
        @Volatile
        private var INSTANCE: DayAdapter? = null

        fun getInstance(context: Context): DayAdapter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DayAdapter(context).also { INSTANCE = it }
            }
        }

        fun getInstance(): DayAdapter? {
          return INSTANCE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DayRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dayItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thisDay = dayItems[position]
        holder.binding.tvDate.text = thisDay.YearMonthDay
        
        val hoursStudied = thisDay.TimeStudiedSec / 3600.0
        val goalHours = thisDay.GoalTimeSec / 3600.0
        holder.binding.tvTimeStudied.text = String.format("%.1fh/%.1fh", hoursStudied, goalHours)

        val progress = if (thisDay.GoalTimeSec > 0) {
            (thisDay.TimeStudiedSec.toFloat() / thisDay.GoalTimeSec * 100).toInt()
        } else {
            0
        }
        holder.binding.progressBar.progress = progress

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
            AppDatabase.getInstance(context).dayDao().deleteDay(dayItems[position])
            (context as MainActivity).runOnUiThread {
                dayItems.removeAt(position)
                notifyItemRemoved(position)
            }
        }.start()
    }

    fun addDay(day: Day) {
        Thread {
            AppDatabase.getInstance(context).dayDao().insertDay(day)
            (context as MainActivity).runOnUiThread {
                dayItems.add(day)
                notifyItemInserted(dayItems.lastIndex)
            }
        }.start()
    }

    fun updateDay(day: Day, editIndex: Int) {
        Thread {
            AppDatabase.getInstance(context).dayDao().updateDay(day)
            (context as MainActivity).runOnUiThread {
                dayItems[editIndex] = day
                notifyItemChanged(editIndex)
            }
        }.start()
    }

    fun addSecStudied(seconds: Int) {
        Thread {
            val date = LocalDate.now().toString()
            AppDatabase.getInstance(context).dayDao().addSecStudied(date, seconds) 
            (context as MainActivity).runOnUiThread {
                val dayIndex = dayItems.indexOfFirst { it.YearMonthDay == date }
                if (dayIndex != -1) {
                    dayItems[dayIndex].TimeStudiedSec += seconds
                    notifyItemChanged(dayIndex)
                }
            }
        }.start()
    }

    override fun onDismissed(position: Int) {
        deleteDay(position)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(dayItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }
}
