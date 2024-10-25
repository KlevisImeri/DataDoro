package hu.bme.aut.t4xgko.DataDoro.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
  tableName = "Day"
  indices = [Index(value = ["YearMonthDay"], unique = true)] 
)
data class Day(
        @PrimaryKey(autoGenerate = true) var DayId: Long?,
        @ColumnInfo(name = "YearMonthDay") var YearMonthDay: String,
        @ColumnInfo(name = "Text") var dayText: String,
        @ColumnInfo(name = "Image") var image: ByteArray?,
        @ColumnInfo(name = "TimeStudiedMin") var TimeStudiedMin: Int,
) : Serializable {
  // fun setDate(localDate: LocalDate) {
  //   yearMonthDay = localDate.toString()
  // }
}
