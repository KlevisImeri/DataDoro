package hu.bme.aut.t4xgko.DataDoro

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.databinding.ActivityDayBinding
import hu.bme.aut.t4xgko.DataDoro.network.WeatherAPI
import hu.bme.aut.t4xgko.DataDoro.permissionHandlers.LocationPermissionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import hu.bme.aut.t4xgko.DataDoro.network.WeatherAPI.WeatherInfo
import java.util.Locale

class DayActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDayBinding
    private var day: Day = Day.nullDay()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imagePath = result.data?.getStringExtra("IMAGE_PATH")
            imagePath?.let {
                deleteOldImage()
                day.image = it
                updateUI()
            }
        }
    }

    private lateinit var locationPermissionHandler: LocationPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDay()

        locationPermissionHandler = LocationPermissionHandler(this)
        locationPermissionHandler.setupPermissions {
            fetchLocationAndWeather()
        }
        
        setupClickListeners()
    }

    private fun setupDay() {
        val yearMonthDay = intent.getStringExtra("DAY_ID") ?: run {
            finish()
            return
        }
        
        Thread {
            val loadedDay = AppDatabase.getInstance(this@DayActivity).dayDao().getDay(yearMonthDay)
            day = loadedDay ?: Day.nullDay()
            updateUI()
            locationPermissionHandler.checkAndRequestPermissions()
            runOnUiThread{
            }
        }.start()

        
    }

    private fun setupClickListeners() {
        with(binding) {
            btnSave.setOnClickListener { saveDay(); finish() }
            btnCamera.setOnClickListener { launchCamera() }
            imageView.setOnClickListener { showFullscreenImage() }
        }
    }

    private fun fetchLocationAndWeather() {
        if (!(day.City == null || day.Temperature == null)) return
        val location = locationPermissionHandler.getLastKnownLocation()
        if (location == null) return
        WeatherAPI.getWeatherInfo(location) { weatherInfo ->
            runOnUiThread {
                if (weatherInfo != null) {
                    day.Temperature = weatherInfo.averageTemp
                    day.City = weatherInfo.cityName
                    saveDay()
                    updateUI()
                } else {
                    Toast.makeText(this, 
                        "Failed to fetch weather data! You dont have internet", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUI() {
        runOnUiThread {
            with(binding) {
                tvDate.text = day.YearMonthDay
                editTextDate.setText(day.dayText)
                progressBar.apply {
                    max = day.GoalTimeSec
                    progress = day.TimeStudiedSec
                }
                day.image?.let { imagePath ->
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    imageView.setImageBitmap(bitmap)
                }
                val hoursStudied = day.TimeStudiedSec / 3600.0
                val goalHours = day.GoalTimeSec / 3600.0
                tvTimeStudied.text = String.format(Locale.getDefault(),"%.1fh/%.1fh", hoursStudied, goalHours)
                binding.tvWeather.text = 
                if (day.City == null && day.Temperature == null) {
                    ""
                } else {
                    "${day.City ?: ""} | ${day.Temperature?.let { "$it°C" } ?: ""}"
                }
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private fun saveDay() {
        Thread {
            day.dayText = binding.editTextDate.text.toString()
            AppDatabase.getInstance(this).dayDao().updateDay(day)
        }.start()
    }

    private fun showFullscreenImage() {
        day.image?.let { imagePath ->
            val dialog = FullscreenImageDialogFragment.newInstance(imagePath)
            dialog.show(supportFragmentManager, "FullscreenImageDialog")
        }
    }

    private fun deleteOldImage() {
        day.image?.let { oldImagePath ->
            val oldImageFile = File(oldImagePath)
            if (oldImageFile.exists()) {
                oldImageFile.delete()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveDay()
    }
}
