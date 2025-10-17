package com.alvin.nutrigrow.ui.aigrow

import android.net.Uri
import android.os.Bundle
import android.text.Html
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
import java.io.File

class AIGrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAigrowBinding
    private val vm: AIGrowViewModel by viewModels()
    private var selectedUri: Uri? = null
    private var currentDiagnosis: Diagnosis? = null

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
        binding = ActivityAigrowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "AI Grow"
        }

        binding.btnFromCamera.setOnClickListener { openCamera() }
        binding.btnFromGallery.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.btnSaveDiagnosis.setOnClickListener {
            currentDiagnosis?.let { vm.saveDiagnosis(it) }
        }

        observeVM()
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
        val file = File(externalCacheDir, "temp_aigrow.jpg")
        selectedUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraLauncher.launch(selectedUri)
    }

    private fun previewAndUpload(uri: Uri) {
        selectedUri = uri
        Glide.with(this).load(uri).into(binding.imgAIPosted)
        binding.imgAIPosted.visibility = View.VISIBLE

        vm.uploadImage(uri) { imageUrl ->
            vm.diagnose(imageUrl)
        }
    }

    private fun observeVM() {
        vm.isLoading.observe(this) { loading ->

        }

        vm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }

        vm.diagnosisResult.observe(this) { diag ->
            diag?.let {
                currentDiagnosis = it
                binding.tvAIResponse.text = Html.fromHtml(it.response, Html.FROM_HTML_MODE_COMPACT)
                binding.btnSaveDiagnosis.visibility = View.VISIBLE
            }
        }
    }
}