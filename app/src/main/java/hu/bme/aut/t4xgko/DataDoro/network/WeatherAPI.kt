package hu.bme.aut.t4xgko.DataDoro.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import kotlin.math.roundToInt

class WeatherAPI {
    private data class WeatherResponse(
        val main: Main
    )
    
    private data class Main(
        val temp_min: Double,
        val temp_max: Double
    )

    private interface WeatherApiService {
        @GET("data/2.5/weather")
        fun getWeather(
            @Query("q") cityName: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): Call<WeatherResponse>
    }

    companion object {
        private const val API_KEY = "e8f84752c21df290825c05cab35be666"
        private const val BASE_URL = "https://api.openweathermap.org/"

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val api: WeatherApiService = retrofit.create(WeatherApiService::class.java)

        fun getAverageTemperature(cityName: String, callback: (Int?) -> Unit) {
            Thread {
                try {
                    val response = api.getWeather(cityName, API_KEY, "metric").execute()
                    if (response.isSuccessful && response.body() != null) {
                        val weatherResponse = response.body()!!
                        val averageTemp = (weatherResponse.main.temp_min + weatherResponse.main.temp_max) / 2
                        callback(averageTemp.roundToInt())
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            }.start()
        }
    }
}
