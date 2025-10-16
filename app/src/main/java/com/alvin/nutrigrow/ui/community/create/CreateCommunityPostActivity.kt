package com.alvin.nutrigrow.ui.community.create

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.alvin.nutrigrow.databinding.ActivityCreateCommunityPostBinding
import com.alvin.nutrigrow.ui.community.CommunityViewModel
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class CreateCommunityPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCommunityPostBinding
    private val viewModel: CommunityViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                Glide.with(this).load(it).into(binding.imgPickedCommunityImagePreview)
                binding.imgPickedCommunityImagePreview.visibility = android.view.View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCommunityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Buat Postingan Baru"
        }

        setListener()
        observeViewModel()
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

    private fun setListener() {
        binding.btnPickCommunityImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnCreateCommunity.setOnClickListener {
            val title = binding.etCommunityTitle.text.toString().trim()
            val content = binding.etContentCommunity.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Title dan content tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Silakan pilih gambar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadToCloudinary()
        }
    }

    private fun uploadToCloudinary() {
        selectedImageUri?.let { uri ->
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.btnCreateCommunity.isEnabled = false
            MediaManager.get().upload(uri)
                .unsigned("nutrigrow")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {

                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {

                    }

                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        val imageUrl = resultData["secure_url"]?.toString() ?: ""
                        viewModel.createPost(
                            title = binding.etCommunityTitle.text.toString().trim(),
                            content = binding.etContentCommunity.text.toString().trim(),
                            imageUrl = imageUrl
                        )
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(
                            this@CreateCommunityPostActivity,
                            "Upload gagal: ${error.description}",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnCreateCommunity.isEnabled = true
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {

                    }
                })
                .dispatch()
        } ?: run {
            Toast.makeText(this, "Gambar tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.btnCreateCommunity.isEnabled = false
            } else {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnCreateCommunity.isEnabled = true
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Postingan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}