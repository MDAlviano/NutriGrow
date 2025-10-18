package com.alvin.nutrigrow.ui.plantplan

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.MyApplication
import com.alvin.nutrigrow.data.Diagnosis
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.data.Progress
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PlantPlanViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val gemini: GenerativeModel = MyApplication.geminiModel

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> get() = _plans

    private val _progresses = MutableLiveData<List<Progress>>()
    val progresses: LiveData<List<Progress>> get() = _progresses

    private val _progressResult = MutableLiveData<Progress?>()
    val progressResult: LiveData<Progress?> get() = _progressResult

    private val _canUploadToday = MutableLiveData<Boolean>()
    val canUploadToday: LiveData<Boolean> get() = _canUploadToday

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchUserPlans(growingMedia: String? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            _isLoading.postValue(false)
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = db.collection("Plans").whereEqualTo("userId", userId)
                val snapshot = if (growingMedia != null && growingMedia != "Semua") {
                    query.whereEqualTo("growingMedia", growingMedia).get().await()
                } else {
                    query.get().await()
                }
                val planList = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Plan::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("fetchUserPlans", "Error parsing document: ${e.message}", e)
                        null
                    }
                }
                Log.d("fetchUserPlans", "Fetched ${planList.size} plans for user $userId")
                _plans.postValue(planList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("fetchUserPlans", "Error fetching plans: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchPlanProgresses(planId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Progress")
                    .whereEqualTo("plantPlanId", planId)
                    .get()
                    .await()
                val progressList = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Progress::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                _progresses.postValue(progressList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("plant plan vm fetch plant progress", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createPlan(name: String, growingMedia: String, plant: String, condition: String = "") {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            return
        }

        if (name.isBlank() || plant.isBlank()) {
            _error.postValue("Name, plant cannot be empty")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val plan = hashMapOf(
                    "userId" to userId,
                    "name" to name,
                    "growingMedia" to growingMedia,
                    "plant" to plant,
                    "day" to 0,
                    "condition" to condition,
                    "createdAt" to Timestamp.now()
                )
                db.collection("Plans").add(plan).await()
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("plant plan vm fetch create plan", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit) {
        _isLoading.value = true
        MediaManager.get().upload(uri)
            .option("upload_preset", "nutrigrow")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                    val url = resultData["secure_url"]?.toString().orEmpty()
                    onSuccess(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    _error.postValue("Upload gagal: ${error.description}")
                    Log.e("plant plan vm upload image", error.description)
                    _isLoading.postValue(false)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun analyzeProgress(imageUrl: String, plantPlanId: String, day: Int, plant: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = """
                    Kamu adalah ahli agronomi yang berpengalaman dalam mendiagnosis penyakit tanaman berdasarkan kondisi tanaman nya.

                    Tugasmu adalah mengidentifikasi tanaman berdasarkan deskripsi atau data yang diberikan. Tanamannya adalah $plant. Deskripsikanlah kondisi tanaman tersebut. Jika tanaman tersebut buah/ sayur maka tolong beri tahu apakah tanaman tersebut sudah siap panen atau belum. Jika sudah siap panen, tolong beritahu user di dalam atribut kondisi.   
                    Jawab **dalam format JSON**, dengan dua bagian:
                    1. "title": berupa judul
                    2. "response": berupa teks HTML yang berisi:
                       - deskripsi kondisi tanaman dan penyakitnya jika ada (gunakan <p> untuk paragraf) dan apakah 
                       - identifikasi masalah yang spesifik (gunakan <ul><li> untuk daftar)
                       - solusi atau penanganan organik tanpa bahan kimia (gunakan <ol><li>)
                    3. "kondisi": berisi satu kata untuk menggambarkan tingkat kesehatan tanaman ("baik", "sedang", "buruk", atau "darurat").

                    Pastikan format JSON valid dan tidak keluar dari struktur berikut:

                    {
                      "title": "Judul Diagnosa",
                      "response": "<p>Deskripsi...</p><ul><li>Masalah 1...</li></ul><ol><li>Solusi 1...</li></ol>",
                      "kondisi": "sedang"
                    }

                    Url Gambar:
                    $imageUrl
                """.trimIndent()

                val content = content { text(prompt) }
                val response = gemini.generateContent(content)
                val jsonStr = response.text?.trim() ?: throw IllegalStateException("Empty response from Gemini")
                Log.d("progress check", "Received Gemini response: $jsonStr")

                // Hapus markup markdown (```json dan ```)
                val cleanedJsonStr = jsonStr
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                Log.d("AIGrowViewModel", "Cleaned JSON response: $cleanedJsonStr")

                // Validasi JSON
                if (cleanedJsonStr.isEmpty() || cleanedJsonStr == "{}") {
                    throw IllegalStateException("Invalid JSON response from Gemini")
                }

                try {
                    val json = JSONObject(cleanedJsonStr)
                    val title = json.optString("title", "Diagnosa Tanaman")
                    val htmlResponse = json.optString("response", "<p>Tidak ada respons</p>")
                    val kondisi = json.optString("kondisi", "tidak_diketahui")

                    val progress = Progress(
                        id = "",
                        day = day,
                        response = htmlResponse,
                        condition = kondisi,
                        imageUrl = imageUrl,
                        createdAt = Timestamp.now(),
                        plantPlanId = plantPlanId,
                    )
                    _progressResult.postValue(progress)
                    _error.postValue(null)
                } catch (e: JSONException) {
                    Log.e("AIGrowViewModel", "JSON parsing error: ${e.message}", e)
                    _error.postValue("Gagal memproses respons AI: ${e.message}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Gemini error")
                Log.e("plant plan vm analyze progress", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveProgress(progress: Progress) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "plantPlanId" to progress.plantPlanId,
                    "day" to progress.day,
                    "imageUrl" to progress.imageUrl,
                    "response" to progress.response,
                    "condition" to progress.condition,
                    "createdAt" to Timestamp.now()
                )
                db.collection("Plans")
                    .document(progress.plantPlanId)
                    .collection("Progress")
                    .add(data)
                    .await()

                db.collection("Plans")
                    .document(progress.plantPlanId)
                    .update("day", FieldValue.increment(1))
                    .await()

                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("plant plan vm save progress", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun checkCanUploadToday(planId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val snapshot = db.collection("Plans")
                    .document(planId)
                    .collection("Progress")
                    .whereGreaterThanOrEqualTo("createdAt", Timestamp(todayStart))
                    .get()
                    .await()
                _canUploadToday.postValue(snapshot.isEmpty)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("plant plan vm check can upload today", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}