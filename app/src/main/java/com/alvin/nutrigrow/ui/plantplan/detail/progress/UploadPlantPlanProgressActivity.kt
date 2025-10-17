package com.alvin.nutrigrow.ui.plantplan.detail.progress

import android.net.Uri
import android.os.Bundle
import android.text.Html
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

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && selectedUri != null) {
            previewAndUpload(selectedUri!!)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { previewAndUpload(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Upload Progress"
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnFromCamera.setOnClickListener { openCamera() }
        binding.btnFromGallery.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.btnSaveProgress.setOnClickListener {
            currentProgress?.let { viewModel.saveProgress(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)

    private fun openCamera() {
        val file = File(externalCacheDir, "temp_progress.jpg")
        selectedUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraLauncher.launch(selectedUri!!)
    }

    private fun previewAndUpload(uri: Uri) {
        selectedUri = uri
        Glide.with(this).load(uri).into(binding.imgAIPosted)
        binding.imgAIPosted.visibility = View.VISIBLE

        val plan = intent.getParcelableExtra<Plan>("PLAN") ?: return
        val day = plan.day + 1
        viewModel.uploadImage(uri) { imageUrl ->
            viewModel.analyzeProgress(imageUrl, plan.id, day, plan.plant)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->

        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.progressResult.observe(this) { progress ->
            progress?.let {
                currentProgress = it
                binding.tvAIResponse.text = Html.fromHtml(it.response, Html.FROM_HTML_MODE_COMPACT)
                binding.btnSaveProgress.visibility = View.VISIBLE
            }
        }
    }
}