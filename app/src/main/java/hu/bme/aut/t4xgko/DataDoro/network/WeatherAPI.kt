package hu.bme.aut.t4xgko.DataDoro.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import kotlin.math.roundToInt
import android.location.Location

class WeatherAPI {
    private data class WeatherResponse(
        val main: Main,
        val name: String,
        val sys: Sys
    )
   
    private data class Main(
        val temp_min: Double,
        val temp_max: Double
    )

    private data class Sys(
        val country: String
    )
   
    private interface WeatherApiService {
        @GET("data/2.5/weather")
        fun getWeatherByCoordinates(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
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

        fun getWeatherInfo(location: Location, callback: (WeatherInfo?) -> Unit) {
            Thread {
                try {
                    val response = api.getWeatherByCoordinates(
                        location.latitude, 
                        location.longitude, 
                        API_KEY, 
                        "metric"
                    ).execute()
                    
                    if (response.isSuccessful && response.body() != null) {
                        val weatherResponse = response.body()!!
                        val averageTemp = (weatherResponse.main.temp_min + weatherResponse.main.temp_max) / 2
                        
                        val weatherInfo = WeatherInfo(
                            cityName = "${weatherResponse.name}, ${weatherResponse.sys.country}",
                            averageTemp = averageTemp.roundToInt()
                        )
                        
                        callback(weatherInfo)
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            }.start()
        }
    }

    data class WeatherInfo(
        val cityName: String,
        val averageTemp: Int
    )
}