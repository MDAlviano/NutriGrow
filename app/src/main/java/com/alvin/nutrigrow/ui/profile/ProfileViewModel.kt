package com.alvin.nutrigrow.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.CommunityPost
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<CommunityPost>>()
    val posts: LiveData<List<CommunityPost>> get() = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchUserPosts() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            _isLoading.postValue(false)
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Posts")
                    .whereEqualTo("author", userId)
                    .get()
                    .await()
                val postList = snapshot.documents.mapNotNull { document ->
                    try {
                        val post = document.toObject(CommunityPost::class.java)?.copy(id = document.id)
                        post?.let {
                            // Format date to "dd MMMM yyyy"
                            val rawDate = document.getString("date") ?: ""
                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                val parsedDate = inputFormat.parse(rawDate)
                                parsedDate?.let { outputFormat.format(it) } ?: rawDate
                            } catch (e: Exception) {
                                rawDate // Fallback to raw date if parsing fails
                            }
                            it.copy(date = formattedDate)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                _posts.postValue(postList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}