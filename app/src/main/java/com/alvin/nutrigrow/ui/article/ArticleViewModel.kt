package com.alvin.nutrigrow.ui.article

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.Article
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Articles").get().await()
                val categories = snapshot.documents
                    .mapNotNull { it.getString("category") }
                    .distinct()
                    .sorted()
                _categories.postValue(categories)
            } catch (e: Exception) {
                Log.e("article vm fetch categories", e.message.toString())
                _error.postValue("Failed to fetch categories: ${e.message}")
            }
        }
    }

    fun fetchArticles(category: String? = null, searchQuery: String? = null) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var query: Query = db.collection("Articles")
                if (category != null && category.isNotBlank()) {
                    query = query.whereEqualTo("category", category)
                }
                val snapshot = query.get().await()
                val articleList = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Article::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("fetchArticles", "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }.let { articles ->
                    if (searchQuery != null && searchQuery.isNotBlank()) {
                        articles.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    } else {
                        articles
                    }
                }
                Log.d("fetchArticles", "Fetched ${articleList.size} articles")
                _articles.postValue(articleList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("fetchArticles", "Error fetching articles: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchRecommendedArticles(excludeArticleId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Articles")
//                    .limit(5)
                    .get()
                    .await()
                val articleList = snapshot.documents.mapNotNull { document ->
                    try {
                        if (document.id == excludeArticleId) null
                        else document.toObject(Article::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("fetchRecommendedArticles", "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }
                Log.d("fetchRecommendedArticles", "Fetched ${articleList.size} recommended articles")
                _recommendedArticles.postValue(articleList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("queryIndexAnjay", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}