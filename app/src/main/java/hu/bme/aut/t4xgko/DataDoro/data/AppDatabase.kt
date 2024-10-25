package hu.bme.aut.t4xgko.DataDoro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Day::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun DayDao(): DayDAO

  companion object {
    private var INSTANCE: AppDatabase? = null
    fun getInstance(context: Context): AppDatabase {
      if (INSTANCE == null) {
        INSTANCE =
                Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase::class.java,
                                "Days.db"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
      }
      return INSTANCE!!
    }

    fun destroyInstance() {
      INSTANCE = null
    }
  }
}
