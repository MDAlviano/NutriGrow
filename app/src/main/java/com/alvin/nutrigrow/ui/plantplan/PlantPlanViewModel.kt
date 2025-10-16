package com.alvin.nutrigrow.ui.plantplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.data.Progress
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class PlantPlanViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> get() = _plans

    private val _progresses = MutableLiveData<List<Progress>>()
    val progresses: LiveData<List<Progress>> get() = _progresses

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
                        val plan = document.toObject(Plan::class.java)?.copy(id = document.id)
                        plan?.let {
                            val formattedDate = it.createdAt?.let { date ->
                                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                outputFormat.format(date)
                            } ?: ""
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

    fun fetchPlanProgresses(planId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("Plans")
                    .document(planId)
                    .collection("Progress")
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

        if (name.isBlank() || plant.isBlank() || condition.isBlank()) {
            _error.postValue("Name, plant, and condition cannot be empty")
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
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}