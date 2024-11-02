package hu.bme.aut.t4xgko.DataDoro.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "Day",
    indices = [Index(value = ["YearMonthDay"], unique = true)]
)
data class Day(
    @PrimaryKey(autoGenerate = true) var DayId: Long? = null,
    @ColumnInfo(name = "YearMonthDay") var YearMonthDay: String = "",
    @ColumnInfo(name = "Text") var dayText: String = "",
    @ColumnInfo(name = "Image") var image: String? = null,
    @ColumnInfo(name = "TimeStudiedSec") var TimeStudiedSec: Int = 0,
    @ColumnInfo(name = "GoalTimeSec") var GoalTimeSec: Int = 8*60*60 
) : Serializable {
    companion object {
        fun nullDay() = Day(
            DayId = null,
            YearMonthDay = "This Day does not exist",
            dayText = "This Day does not exist",
            image = null,
            TimeStudiedSec = 0,
            GoalTimeSec = 0
        )
    }
}

