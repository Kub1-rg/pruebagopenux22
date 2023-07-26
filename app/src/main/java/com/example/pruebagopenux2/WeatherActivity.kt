package com.example.pruebagopenux2
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.pruebagopenux2.databinding.WeatherBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherActivity : ComponentActivity() {

    lateinit var binding: WeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ApiKey = "e3d3795f74cd75a102f6ae7453201e96"
        val objetoIntent: Intent = intent
        val ciudad: String = objetoIntent.getStringExtra("Ciudades") ?: ""

        binding.txtCiudad.text = "Ciudad: $ciudad"

        searchByCity(ciudad, ApiKey)

        val buttonBack = findViewById<Button>(R.id.buttonVolver)
        buttonBack.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la actividad anterior (MainActivity)
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/geo/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun searchByCity(ciudad: String, apiKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(ApiService::class.java).getLatAndLon(ciudad, apiKey = apiKey)
                val response = call.execute()

                if (response.isSuccessful) {
                    val cityResponse = response.body()
                    if (cityResponse != null && cityResponse.isNotEmpty()) {
                        val lat = cityResponse[0].lat
                        val lon = cityResponse[0].lon

                        WeatherBody(apiKey, lat, lon)
                    } else {
                        runOnUiThread {
                            showError("La respuesta de la API no contiene datos de coordenadas para la ciudad.")
                        }
                    }
                } else {
                    runOnUiThread {
                        showError("Error en la llamada a la API de coordenadas: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Error en la llamada a la API: ${e.message}")
                }
            }
        }
    }

    private fun getRetrofi2(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/3.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun WeatherBody(apiKey: String, lat: Double?, lon: Double?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (lat != null && lon != null) {
                try {
                    val call = getRetrofi2().create(ApiServiceWeather::class.java).getWeather(lat, lon, apiKey)
                    val response = call.execute()

                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            val temp = weatherResponse.current.temp.toString()
                            val feels_like = weatherResponse.current.feels_like.toString()
                            val humidity = weatherResponse.current.humidity.toString()

                            runOnUiThread {
                                binding.txtTmpActual.text = temp
                                binding.txtHumedad.text = humidity
                                binding.txtSensacion.text = feels_like
                            }
                        } else {
                            runOnUiThread {
                                showError("La respuesta de la API de clima está vacía.")
                            }
                        }
                    } else {
                        runOnUiThread {
                            showError("Error en la llamada a la API de clima: ${response.message()}")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        showError("Error en la llamada a la API de clima: ${e.message}")
                    }
                }
            }
        }
    }

    private fun showError(errorMessage: String) {
        Log.e("WeatherActivity", errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}
