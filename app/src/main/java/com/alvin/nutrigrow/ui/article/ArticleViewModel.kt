package com.alvin.nutrigrow.ui.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.Article
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ArticleViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    private val _recommendedArticles = MutableLiveData<List<Article>>()
    val recommendedArticles: LiveData<List<Article>> get() = _recommendedArticles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchArticles() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("article").get().await()
                val articleList = snapshot.documents.mapNotNull { document ->
                    try {
                        val article = document.toObject(Article::class.java)?.copy(id = document.id)
                        article?.let {
                            val rawDate = document.getString("date") ?: ""
                            val formattedDate = try {
                                val inputFormat =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat =
                                    SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val parsedDate = inputFormat.parse(rawDate)
                                parsedDate?.let { outputFormat.format(it) } ?: rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                            it.copy(date = formattedDate)
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

    fun fetchRecommendedArticles(excludeArticleId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("article")
                    .whereNotEqualTo("id", excludeArticleId)
                    .limit(5)
                    .get()
                    .await()
                val articleList = snapshot.documents.mapNotNull { document ->
                    try {
                        val article = document.toObject(Article::class.java)?.copy(id = document.id)
                        article?.let {
                            val rawDate = document.getString("date") ?: ""
                            val formattedDate = try {
                                val inputFormat =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat =
                                    SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val parsedDate = inputFormat.parse(rawDate)
                                parsedDate?.let { outputFormat.format(it) } ?: rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                            it.copy(date = formattedDate)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                _recommendedArticles.postValue(articleList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}