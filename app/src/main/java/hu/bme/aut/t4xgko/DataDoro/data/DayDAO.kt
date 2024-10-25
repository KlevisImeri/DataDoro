package hu.bme.aut.t4xgko.DataDoro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DayDAO {
  @Query("SELECT * FROM Day") fun getAllDays(): List<Day>
  @Insert(onConflict = OnConflictStrategy.IGNORE) fun insertDay(day: Day): Long
  @Delete fun deleteDay(day: Day)
  @Update fun updateDay(day: Day)
  @Query("SELECT * FROM Day WHERE YearMonthDay = :currentDate LIMIT 1")
  fun getDayByDate(currentDate: String): Day?
}
