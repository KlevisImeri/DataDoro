package hu.bme.aut.t4xgko.DataDoro

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import hu.bme.aut.dayrecyclerviewdemo.adapter.DayAdapter
import hu.bme.aut.dayrecyclerviewdemo.data.AppDatabase
import hu.bme.aut.dayrecyclerviewdemo.data.Day
import hu.bme.aut.dayrecyclerviewdemo.databinding.ActivityMainBinding
import hu.bme.aut.dayrecyclerviewdemo.touch.DayRecyclerTouchCallback
import java.util.Date
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MainActivity : AppCompatActivity(), DayDialog.DayHandler {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private lateinit var binding: ActivityMainBinding
  private lateinit var dayAdapter: DayAdapter

  companion object {
    const val KEY_EDIT = "KEY_EDIT"
    const val PREF_NAME = "PREFTODO"
    const val KEY_STARTED = "KEY_STARTED"
    const val KEY_LAST_USED = "KEY_LAST_USED"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)

    binding.fab.setOnClickListener { view -> showAddDayDialog() }

    if (!wasStartedBefore()) {
      MaterialTapTargetPrompt.Builder(this)
              .setTarget(R.id.fab)
              .setPrimaryText("Create day")
              .setSecondaryText("Click here to create new items")
              .show()
    }
    Thread {
              AppDatabase.getInstance(this).dayDao().insertDay(Day(
                DayId = null, 
                yearMonthDay = localDate.format(formatter), 
                dayText = "", 
                image = null, 
                TimeStudiedMin = 0 
              ))
              var dayList = AppDatabase.getInstance(this).dayDao().getAllDays()
              runOnUiThread {
                dayAdapter = DayAdapter(this, dayList)
                binding.recyclerDay.adapter = dayAdapter

                val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
                binding.recyclerDay.addItemDecoration(itemDecoration)

                // binding.recyclerDay.layoutManager = GridLayoutManager(this, 2)
                // binding.recyclerDay.layoutManager = StaggeredGridLayoutManager(2,
                // StaggeredGridLayoutManager.VERTICAL)
                val touchCallbakList = DayRecyclerTouchCallback(dayAdapter)
                val itemTouchHelper = ItemTouchHelper(touchCallbakList)
                itemTouchHelper.attachToRecyclerView(binding.recyclerDay)
              }
            }
            .start()
    saveStartInfo()
  }

  fun saveStartInfo() {
    var sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    var editor = sharedPref.edit()
    editor.putBoolean(KEY_STARTED, true)
    editor.putString(KEY_LAST_USED, Date(System.currentTimeMillis()).toString())
    editor.apply()
  }

  fun wasStartedBefore(): Boolean {
    var sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    var lastTime = sharedPref.getString(KEY_LAST_USED, "This is the first time")
    Toast.makeText(this, lastTime, Toast.LENGTH_LONG).show()
    return sharedPref.getBoolean(KEY_STARTED, false)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_settings -> true
      else -> super.onOptionsItemSelected(item)
    }
  }

  fun showAddDayDialog() {
    DayDialog().show(supportFragmentManager, "Dialog")
  }
  private fun saveDay(day: Day) {
    Thread {
              day.dayId = AppDatabase.getInstance(this).dayDao().insertDay(day)
              runOnUiThread { dayAdapter.addDay(day) }
            }
            .start()
  }
  override fun dayCreated(day: Day) {
    saveDay(day)
  }

  var editIndex: Int = -1
  public fun showEditDayDialog(dayToEdit: Day, index: Int) {
    editIndex = index
    val editItemDialog = DayDialog()
    val bundle = Bundle()
    bundle.putSerializable(KEY_EDIT, dayToEdit)
    editItemDialog.arguments = bundle
    editItemDialog.show(supportFragmentManager, "EDITDIALOG")
  }

  override fun dayUpdated(day: Day) {
    Thread {
              AppDatabase.getInstance(this).dayDao().updateDay(day)
              runOnUiThread { dayAdapter.updateDay(day, editIndex) }
            }
            .start()
  }
}
