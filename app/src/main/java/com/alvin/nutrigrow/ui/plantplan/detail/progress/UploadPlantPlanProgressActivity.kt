package com.alvin.nutrigrow.ui.plantplan.detail.progress

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.ActivityUploadPlantPlanProgressBinding
import com.alvin.nutrigrow.ui.plantplan.PlantPlanViewModel
import com.bumptech.glide.Glide
import java.io.File

class UploadPlantPlanProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadPlantPlanProgressBinding
    private val viewModel: PlantPlanViewModel by viewModels()
    private var selectedUri: Uri? = null
    private var currentProgress: Progress? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && selectedUri != null) {
                Log.d("UploadPlantPlanProgress", "Camera capture successful, URI: $selectedUri")
                previewAndUpload(selectedUri!!)
            } else {
                Log.w("UploadPlantPlanProgress", "Camera capture failed or URI is null")
                Toast.makeText(this, "Gagal mengambil gambar dari kamera", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Log.d("UploadPlantPlanProgress", "Gallery image selected, URI: $uri")
                previewAndUpload(uri)
            } else {
                Log.w("UploadPlantPlanProgress", "No image selected from gallery")
                Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPlantPlanProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupListeners()
        observeViewModel()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Upload Progress Tanaman"
        }
    }

    private fun setupListeners() {
        binding.btnFromCamera.setOnClickListener { openCamera() }
        binding.btnFromGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnSaveProgress.setOnClickListener {
            currentProgress?.let {
                Log.d("UploadPlantPlanProgress", "Saving progress: ${it.day}, ${it.condition}")
                viewModel.saveProgress(it)
                Toast.makeText(this, "Progress disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } ?: run {
                Log.w("UploadPlantPlanProgress", "No progress to save")
                Toast.makeText(this, "Tidak ada data progress untuk disimpan", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openCamera() {
        try {
            val file = File(externalCacheDir, "temp_progress_${System.currentTimeMillis()}.jpg")
            selectedUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            cameraLauncher.launch(selectedUri!!)
        } catch (e: Exception) {
            Log.e("UploadPlantPlanProgress", "Error opening camera: ${e.message}", e)
            Toast.makeText(this, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previewAndUpload(uri: Uri) {
        selectedUri = uri
        Glide.with(this)
            .load(uri)
            .error(android.R.drawable.ic_menu_close_clear_cancel)
            .into(binding.imgAIPosted)
        binding.imgAIPosted.visibility = View.VISIBLE
        binding.tvAIResponse.text = ""
        binding.btnSaveProgress.visibility = View.GONE

        val plan = intent.getParcelableExtra<Plan>("PLAN")
        if (plan == null) {
            Log.e("UploadPlantPlanProgress", "No Plan data received from intent")
            Toast.makeText(this, "Data rencana tanam tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("UploadPlantPlanProgress", "Uploading image for plan day: ${plan.day + 1}")
        viewModel.uploadImage(uri) { imageUrl ->
            viewModel.analyzeProgress(imageUrl, plan.id, plan.day + 1, plan.plant)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            Log.d("UploadPlantPlanProgress", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Log.e("UploadPlantPlanProgress", "Error: $it")
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.progressResult.observe(this) { progress ->
            progress?.let {
                Log.d("UploadPlantPlanProgress", "Progress received: ${it.condition}")
                currentProgress = it
                binding.tvAIResponse.text = Html.fromHtml(it.response, Html.FROM_HTML_MODE_COMPACT)
                binding.cardAIResponse.visibility = View.VISIBLE
                binding.btnSaveProgress.visibility = View.VISIBLE
            } ?: run {
                Log.w("UploadPlantPlanProgress", "No progress data received")
                binding.cardAIResponse.visibility = View.GONE
                binding.btnSaveProgress.visibility = View.GONE
            }
        }
    }
}