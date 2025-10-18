package com.alvin.nutrigrow.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.CommunityPost
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<CommunityPost>>()
    val posts: LiveData<List<CommunityPost>> get() = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)
        _currentUser.value = auth.currentUser
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

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
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val postList = snapshot.documents.mapNotNull { document ->
                    try {
                        // Ambil jumlah komentar untuk post ini
                        val commentCount = db.collection("Comments")
                            .whereEqualTo("postId", document.id)
                            .get()
                            .await()
                            .size()
                        document.toObject(CommunityPost::class.java)?.copy(
                            id = document.id,
                            replies = commentCount
                        )
                    } catch (e: Exception) {
                        Log.e("fetchUserPosts", "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }
                Log.d("fetchUserPosts", "Fetched ${postList.size} posts for user $userId")
                _posts.postValue(postList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("fetchUserPosts", "Error fetching posts: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun logout() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.signOut()
                Log.d("HomeViewModel", "User logged out successfully")
                _error.postValue(null)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Logout error: ${e.message}", e)
                _error.postValue("Gagal logout: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}