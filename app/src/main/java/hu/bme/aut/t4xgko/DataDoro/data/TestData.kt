package hu.bme.aut.t4xgko.DataDoro.data

import android.content.Context  
import hu.bme.aut.t4xgko.DataDoro.R
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object TestData {
    var TEST_DATA_SIZE = 5;
    fun insert(context: Context, updateProgressBar: (Int) -> Unit) {
        val dayDao = AppDatabase.getInstance(context).dayDao()

        if (dayDao.getAllDays().isNotEmpty()) {
            return
        }
        
        fun saveImageToFile(resourceId: Int, fileName: String): String {
            val inputStream = context.resources.openRawResource(resourceId)
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            updateProgressBar(100 / TEST_DATA_SIZE)
            return file.absolutePath
        }


        val testDays = listOf(
            Day(
                DayId = null,
                YearMonthDay = LocalDate.now().minusDays(1).toString(),
                dayText = "Test Day 1: Felt very productive and accomplished a lot of tasks.",
                image = saveImageToFile(R.drawable.test_image1, "test_image1.png"),
                TimeStudiedSec = 2 * 60 * 60, // 2 hours
                GoalTimeSec = 8 * 60 * 60, // 8 hours
                Temperature = 17,
                City = "Prishtina"
            ),
            Day(
                DayId = null,
                YearMonthDay = LocalDate.now().minusDays(2).toString(),
                dayText = "Test Day 2: Had a relaxing day, spent time with family and friends.",
                image = saveImageToFile(R.drawable.test_image2, "test_image2.png"),
                TimeStudiedSec = 269 * 60, // 269 minutes
                GoalTimeSec = 6 * 60 * 60, // 6 hours
                Temperature = 10,
                City = "New York"
            ),
            Day(
                DayId = null,
                YearMonthDay = LocalDate.now().minusDays(3).toString(),
                dayText = "Test Day 3: Felt a bit stressed due to work deadlines but managed to push through.",
                image = saveImageToFile(R.drawable.test_image3, "test_image3.png"),
                TimeStudiedSec = 8 * 60 * 60 + 12 * 60, // 8 hours 12 min
                GoalTimeSec = 7 * 60 * 60, // 7 hours
                Temperature = -5,
                City = "Budapest"
            ),
            Day(
                DayId = null,
                YearMonthDay = LocalDate.now().minusDays(4).toString(),
                dayText = "Test Day 4: Enjoyed a peaceful day, went for a walk in the park.",
                image = saveImageToFile(R.drawable.test_image4, "test_image4.png"),
                TimeStudiedSec = 20 * 60, // 20 minutes
                GoalTimeSec = 5 * 60 * 60, // 5 hours
                Temperature = 26,
                City = "Budapest"
            ),
            Day(
                DayId = null,
                YearMonthDay = LocalDate.now().minusDays(5).toString(),
                dayText = "Test Day 5: Spent the day relaxing with a cute cat.",
                image = saveImageToFile(R.drawable.test_image5, "test_image_5.png"),
                TimeStudiedSec = 3 * 60 * 60 + 38 * 60, // 3 h 38 min
                GoalTimeSec = 4 * 60 * 60, // 4 hours
                Temperature = 17,
                City = "London"
            )
        )

        testDays.forEachIndexed { index, day ->
            dayDao.insertDay(day)
        }
    }
}
