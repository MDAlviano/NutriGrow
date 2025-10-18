package com.alvin.nutrigrow.ui.aigrow

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.MyApplication
import com.alvin.nutrigrow.data.Diagnosis
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.ai.client.generativeai.type.content
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AIGrowViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val gemini = MyApplication.geminiModel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _diagnosisResult = MutableLiveData<Diagnosis?>()
    val diagnosisResult: LiveData<Diagnosis?> = _diagnosisResult

    private val _myDiagnosis = MutableLiveData<List<Diagnosis>>()
    val myDiagnosis: LiveData<List<Diagnosis>> = _myDiagnosis

    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit) {
        _isLoading.value = true
        Log.d("AIGrowViewModel", "Starting image upload for URI: $uri")
        MediaManager.get().upload(uri)
            .option("upload_preset", "nutrigrow")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("AIGrowViewModel", "Upload started, requestId: $requestId")
                }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d("AIGrowViewModel", "Upload progress, requestId: $requestId, $bytes/$totalBytes bytes")
                }
                override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                    val url = resultData["secure_url"]?.toString().orEmpty()
                    Log.d("AIGrowViewModel", "Upload success, requestId: $requestId, URL: $url")
                    _isLoading.postValue(false)
                    onSuccess(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("AIGrowViewModel", "Upload failed, requestId: $requestId, error: ${error.description}")
                    _error.postValue("Upload gagal: ${error.description}")
                    _isLoading.postValue(false)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w("AIGrowViewModel", "Upload rescheduled, requestId: $requestId, error: ${error.description}")
                }
            })
            .dispatch()
    }

    fun diagnose(imageUrl: String) {
        _isLoading.value = true
        Log.d("AIGrowViewModel", "Starting diagnosis for image URL: $imageUrl")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = """
                    Kamu adalah ahli agronomi yang berpengalaman dalam mendiagnosis penyakit tanaman berdasarkan kondisi daunnya.

                    Tugasmu adalah mengidentifikasi penyakit pada tanaman berdasarkan deskripsi atau data yang diberikan. 
                    Jawab **dalam format JSON**, dengan tiga bagian:
                    1. "title": berupa judul
                    2. "response": berupa teks HTML yang berisi:
                       - deskripsi kondisi tanaman dan penyakitnya jika ada (gunakan <p> untuk paragraf)
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

                Log.d("AIGrowViewModel", "Sending prompt to Gemini")
                val content = content { text(prompt) }
                val response = gemini.generateContent(content)

                val jsonStr = response.text?.trim() ?: throw IllegalStateException("Empty response from Gemini")
                Log.d("AIGrowViewModel", "Received Gemini response: $jsonStr")

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

                    val diag = Diagnosis(
                        title = title,
                        response = htmlResponse,
                        condition = kondisi,
                        imageUrl = imageUrl,
                        createdAt = Timestamp.now(),
                        userId = auth.currentUser?.uid ?: "",
                        id = ""
                    )
                    Log.d("AIGrowViewModel", "Diagnosis created: title=${diag.title}, condition=${diag.condition}, imageUrl=${diag.imageUrl}")
                    _diagnosisResult.postValue(diag)
                    _error.postValue(null)
                } catch (e: JSONException) {
                    Log.e("AIGrowViewModel", "JSON parsing error: ${e.message}", e)
                    _error.postValue("Gagal memproses respons AI: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("AIGrowViewModel", "Diagnosis error: ${e.message}", e)
                _error.postValue("Gagal melakukan diagnosa: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveDiagnosis(diag: Diagnosis) {
        _isLoading.value = true
        Log.d("AIGrowViewModel", "Saving diagnosis: title=${diag.title}, userId=${diag.userId}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = hashMapOf(
                    "userId" to diag.userId,
                    "title" to diag.title,
                    "imageUrl" to diag.imageUrl,
                    "response" to diag.response,
                    "condition" to diag.condition,
                    "createdAt" to Timestamp.now()
                )
                val docRef = db.collection("diagnosis").add(data).await()
                Log.d("AIGrowViewModel", "Diagnosis saved successfully, docId: ${docRef.id}")
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e("AIGrowViewModel", "Error saving diagnosis: ${e.message}", e)
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchMyDiagnosis() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("AIGrowViewModel", "User not logged in")
            _error.postValue("User not logged in")
            _isLoading.postValue(false)
            return
        }

        _isLoading.value = true
        Log.d("AIGrowViewModel", "Fetching diagnoses for user: $uid")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("diagnosis")
                    .whereEqualTo("userId", uid)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val list = snap.documents.mapNotNull { doc ->
                    try {
                        val diagnosis = doc.toObject(Diagnosis::class.java)?.copy(id = doc.id)
                        Log.d("AIGrowViewModel", "Parsed diagnosis: id=${doc.id}, title=${diagnosis?.title}")
                        diagnosis
                    } catch (e: Exception) {
                        Log.e("AIGrowViewModel", "Error parsing document ${doc.id}: ${e.message}", e)
                        null
                    }
                }
                Log.d("AIGrowViewModel", "Fetched ${list.size} diagnoses for user $uid")
                _myDiagnosis.postValue(list)
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e("AIGrowViewModel", "Error fetching diagnoses: ${e.message}", e)
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}