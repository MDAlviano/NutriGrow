package com.alvin.nutrigrow.ui.aigrow

import android.net.Uri
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
                    _isLoading.postValue(false)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun diagnose(imageUrl: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = """
                    Kamu adalah ahli agronomi yang berpengalaman dalam mendiagnosis penyakit tanaman berdasarkan kondisi daun nya.

                    Tugasmu adalah mengidentifikasi penyakit pada tanaman berdasarkan deskripsi atau data yang diberikan. 
                    Jawab **dalam format JSON**, dengan dua bagian:
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

                val content = content { text(prompt) }
                val response = gemini.generateContent(content)

                val jsonStr = response.text ?: throw IllegalStateException("Empty response")
                val json = JSONObject(jsonStr)

                val title = json.getString("title")
                val htmlResponse = json.getString("response")
                val kondisi = json.getString("kondisi")

                val diag = Diagnosis(
                    title = title,
                    response = htmlResponse,
                    condition = kondisi,
                    imageUrl = imageUrl,
                    createdAt = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                        .format(Date()),
                    userId = auth.currentUser?.uid ?: "",
                    id = ""
                )
                _diagnosisResult.postValue(diag)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Gemini error")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveDiagnosis(diag: Diagnosis) {
        _isLoading.value = true
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
                db.collection("diagnosis").add(data).await()
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchMyDiagnosis() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snap = db.collection("diagnosis")
                    .whereEqualTo("userId", uid)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val list = snap.documents.mapNotNull { doc ->
                    val d = doc.toObject(Diagnosis::class.java)?.copy(id = doc.id)
                    d?.let {
                        // format createdAt (Timestamp → String)
                        val ts = doc.getTimestamp("createdAt")
                        val formatted = ts?.let {
                            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(it.toDate())
                        } ?: ""
                        it.copy(createdAt = formatted)
                    }
                }
                _myDiagnosis.postValue(list)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}