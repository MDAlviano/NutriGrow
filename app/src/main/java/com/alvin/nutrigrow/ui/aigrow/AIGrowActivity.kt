package com.alvin.nutrigrow.ui.aigrow

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Diagnosis
import com.alvin.nutrigrow.databinding.ActivityAigrowBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.io.File

class AIGrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAigrowBinding
    private val vm: AIGrowViewModel by viewModels()
    private var selectedUri: Uri? = null
    private var currentDiagnosis: Diagnosis? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && selectedUri != null) {
            Log.d("AIGrowActivity", "Camera capture success, URI: $selectedUri")
            previewAndUpload(selectedUri!!)
        } else {
            Log.e("AIGrowActivity", "Camera capture failed or URI is null")
            Toast.makeText(this, "Gagal mengambil gambar dari kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Log.d("AIGrowActivity", "Gallery selection success, URI: $uri")
            previewAndUpload(uri)
        } else {
            Log.e("AIGrowActivity", "Gallery selection failed, no URI returned")
            Toast.makeText(this, "Gagal memilih gambar dari galeri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAigrowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "AI Grow"
        }

        binding.btnFromCamera.setOnClickListener {
            Log.d("AIGrowActivity", "Camera button clicked")
            openCamera()
        }
        binding.btnFromGallery.setOnClickListener {
            Log.d("AIGrowActivity", "Gallery button clicked")
            galleryLauncher.launch("image/*")
        }

        binding.btnSaveDiagnosis.setOnClickListener {
            Log.d("AIGrowActivity", "Save diagnosis button clicked, diagnosis: $currentDiagnosis")
            currentDiagnosis?.let {
                vm.saveDiagnosis(it)
                Snackbar.make(binding.root, "Berhasil menyimpan diagnosis", Snackbar.LENGTH_SHORT).show()
            }
        }

        observeVM()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d("AIGrowActivity", "Back button pressed")
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCamera() {
        try {
            val file = File(externalCacheDir, "temp_aigrow.jpg")
            selectedUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            Log.d("AIGrowActivity", "Opening camera with URI: $selectedUri")
            cameraLauncher.launch(selectedUri!!)
        } catch (e: Exception) {
            Log.e("AIGrowActivity", "Error opening camera: ${e.message}", e)
            Toast.makeText(this, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previewAndUpload(uri: Uri) {
        selectedUri = uri
        try {
            Glide.with(this)
                .load(uri)
                .into(binding.imgAIPosted)
            binding.imgAIPosted.visibility = View.VISIBLE
            Log.d("AIGrowActivity", "Image preview set for URI: $uri")
        } catch (e: Exception) {
            Log.e("AIGrowActivity", "Error loading image preview: ${e.message}", e)
            Toast.makeText(this, "Gagal memuat pratinjau gambar", Toast.LENGTH_SHORT).show()
        }

        Log.d("AIGrowActivity", "Starting image upload for URI: $uri")
        vm.uploadImage(uri) { imageUrl ->
            Log.d("AIGrowActivity", "Image uploaded successfully, URL: $imageUrl")
            vm.diagnose(imageUrl)
        }
    }

    private fun observeVM() {
        vm.isLoading.observe(this) { loading ->
            Log.d("AIGrowActivity", "Loading state changed: $loading")
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) { err ->
            err?.let {
                Log.e("AIGrowActivity", "Error observed: $it")
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }

        vm.diagnosisResult.observe(this) { diag ->
            diag?.let {
                currentDiagnosis = it
                binding.tvAIResponse.text = android.text.Html.fromHtml(it.response, android.text.Html.FROM_HTML_MODE_COMPACT)
                binding.btnSaveDiagnosis.visibility = View.VISIBLE
                Log.d("AIGrowActivity", "Diagnosis received: title=${it.title}, condition=${it.condition}")
            }
        }
    }
}