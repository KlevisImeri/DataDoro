package hu.bme.aut.t4xgko.DataDoro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.databinding.ActivityDayBinding

class DayActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDayBinding
  private var day: Day = Day.nullDay()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDayBinding.inflate(layoutInflater)
    setContentView(binding.root)

    var YearMonthDay = intent.getStringExtra("DAY_ID") ?: return

    Thread {
              YearMonthDay?.let { date ->
                val tempday = AppDatabase.getInstance(this).DayDao().getDay(date)
                if (tempday != null) {
                  day = tempday
                }
              }

              runOnUiThread { 
                binding.tvDate.text = day.YearMonthDay 
                binding.editTextDate.setText(day.dayText)
              }
            }
            .start()

    binding.btnSave.setOnClickListener {
      Thread {
                day.dayText = binding.editTextDate.text.toString()
                // day.image = ByteArray(binding.imageView.image)
                AppDatabase.getInstance(this).DayDao().updateDay(day)
                runOnUiThread {
                  val intent = Intent(this, MainActivity::class.java)
                  startActivity(intent)
                  finish()
                }
              }
              .start()
    }
  }
}
