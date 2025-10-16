package com.alvin.nutrigrow.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.data.WeatherResponse
import com.alvin.nutrigrow.service.WeatherApi
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> get() = _plans

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _weather = MutableLiveData<WeatherResult>()
    val weather: LiveData<WeatherResult> get() = _weather

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://api.weatherapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherApi = retrofit.create(WeatherApi::class.java)

    data class WeatherResult(
        val temp: Double,
        val condition: String,
        val advice: String,
        val city: String
    )

    fun fetchWeather(context: Context) {
        _isLoading.value = true
        getUserLocation(context) { city ->
            if (city == "Unknown") {
                _error.value = "Tidak dapat menentukan lokasi"
                _isLoading.value = false
                return@getUserLocation
            }
            val call = weatherApi.getWeather(
                apiKey = R.string.weather_api_key.toString(),
                city = city
            )
            call.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val weather = response.body()
                        val temp = weather?.current?.temp_c ?: 0.0
                        val condition = weather?.current?.condition?.text ?: "Unknown"
                        val advice = if (temp > 20 && (condition == "Clear" || condition == "Partly cloudy")) {
                            "Cocok untuk menanam!"
                        } else {
                            "Sepertinya hari ini bukan waktu terbaik untuk menanam."
                        }
                        _weather.value = WeatherResult(temp, condition, advice, city)

                        val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putFloat("temp", temp.toFloat())
                            putString("condition", condition)
                            putString("advice", advice)
                            putString("city", city)
                            apply()
                        }
                    } else {
                        _error.value = "Gagal mengambil data cuaca"
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    _isLoading.value = false
                    _error.value = "Koneksi gagal: ${t.message}"
                    // Load dari cache jika offline
                    val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
                    val temp = prefs.getFloat("temp", 0f).toDouble()
                    val condition = prefs.getString("condition", "Unknown") ?: "Unknown"
                    val advice = prefs.getString("advice", "Tidak ada data cuaca") ?: "Tidak ada data cuaca"
                    val city = prefs.getString("city", "Unknown") ?: "Unknown"
                    _weather.value = WeatherResult(temp, condition, advice, city)
                }
            })
        }
    }

    private fun getUserLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        val city = addresses?.firstOrNull()?.locality ?: "Unknown"
                        callback(city)
                    } catch (e: Exception) {
                        callback("Unknown")
                    }
                } ?: callback("Unknown")
            }
        } else {
            callback("Unknown")
        }
    }

    fun fetchNewestArticles() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("article")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .await()
                val articleList = snapshot.documents.mapNotNull { document ->
                    try {
                        val article = document.toObject(Article::class.java)?.copy(id = document.id)
                        article?.let {
                            val rawDate = document.getDate("date") ?: ""
                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val parsedDate = inputFormat.parse(rawDate.toString())
                                parsedDate?.let { outputFormat.format(it) } ?: rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                            it.copy(date = formattedDate.toString())
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                _articles.postValue(articleList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchNewestPlans() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            _isLoading.postValue(false)
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Plans")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .await()
                val planList = snapshot.documents.mapNotNull { document ->
                    try {
                        val plan = document.toObject(Plan::class.java)?.copy(id = document.id)
                        plan?.let {
                            val rawDate = document.getString("createdAt") ?: ""
                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val parsedDate = inputFormat.parse(rawDate)
                                parsedDate?.let { outputFormat.format(it) } ?: rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                            it.copy(createdAt = formattedDate)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                _plans.postValue(planList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}