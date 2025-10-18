package com.alvin.nutrigrow.ui.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.Comment
import com.alvin.nutrigrow.data.CommunityPost
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableLiveData<List<CommunityPost>>()
    val posts: LiveData<List<CommunityPost>> get() = _posts

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchPosts() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Ambil semua post
                val postSnapshot = db.collection("Posts").get().await()
                val postList = postSnapshot.documents.mapNotNull { document ->
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
                        Log.e("fetchPosts", "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }
                Log.d("fetchPosts", "Fetched ${postList.size} posts")
                _posts.postValue(postList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("fetchPosts", "Error fetching posts: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createPost(title: String, content: String, imageUrl: String) {
        val userId = auth.currentUser?.uid
        val userName = auth.currentUser?.displayName
        if (userId == null) {
            _error.postValue("User not logged in")
            return
        }

        if (title.isBlank() || content.isBlank()) {
            _error.postValue("Title and content cannot be empty")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentTime = Timestamp.now()
                val post = hashMapOf(
                    "author" to userName,
                    "content" to content,
                    "createdAt" to currentTime,
                    "imageUrl" to imageUrl,
                    "title" to title,
                    "userId" to userId
                )
                db.collection("Posts").add(post).await()
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("community vm create post", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchComments(postId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Comments")
                    .whereEqualTo("postId", postId)
                    .get()
                    .await()
                val commentList = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(Comment::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("fetchComments", "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }
                Log.d("fetchComments", "Fetched ${commentList.size} comments for post $postId")
                _comments.postValue(commentList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("fetchComments", "Error fetching comments: ${e.message}", e)
                Log.e("queryIndexAnjay", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _error.postValue("User not logged in")
            return
        }

        if (commentText.isBlank()) {
            _error.postValue("Comment cannot be empty")
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val comment = hashMapOf(
                    "postId" to postId,
                    "userId" to userId,
                    "comment" to commentText,
                    "createdAt" to Timestamp.now()
                )
                db.collection("Comments").add(comment).await()
                fetchComments(postId)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("community vm add comment", e.message.toString())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}