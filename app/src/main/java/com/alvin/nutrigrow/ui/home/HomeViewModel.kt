package com.alvin.nutrigrow.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.data.Plan
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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