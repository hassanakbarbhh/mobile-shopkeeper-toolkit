package com.shopkeeper.mobileshop.ui.about

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.shopkeeper.mobileshop.BuildConfig
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.FragmentAboutBinding
import com.shopkeeper.mobileshop.utils.AppPreferences
import java.io.File
import java.io.FileOutputStream

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            saveAndApplyPhoto(uri)
        }
    }

    // Fallback for older devices if PickVisualMedia is unavailable
    private val getContentLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            saveAndApplyPhoto(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSavedPhoto()

        val pickPhotoAction = {
            runCatching {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }.onFailure {
                // Fallback to GetContent
                runCatching {
                    getContentLauncher.launch("image/*")
                }.onFailure {
                    Toast.makeText(requireContext(), "Could not open photo picker", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnUploadPhoto.setOnClickListener { pickPhotoAction() }
        binding.btnChangePhotoBadge.setOnClickListener { pickPhotoAction() }
        binding.cardProfilePhoto.setOnClickListener { pickPhotoAction() }

        fun openUrl(url: String) {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }.onFailure {
                Toast.makeText(requireContext(), "Could not open link: $url", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCall.setOnClickListener {
            runCatching {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${getString(R.string.dev_phone)}")))
            }.onFailure {
                Toast.makeText(requireContext(), getString(R.string.dev_phone), Toast.LENGTH_LONG).show()
            }
        }

        binding.btnWhatsApp.setOnClickListener { openUrl(getString(R.string.url_whatsapp)) }
        binding.btnTwitterX.setOnClickListener { openUrl(getString(R.string.url_twitter)) }
        binding.btnFacebook.setOnClickListener { openUrl(getString(R.string.url_facebook)) }
        binding.btnInstagram.setOnClickListener { openUrl(getString(R.string.url_instagram)) }

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.hassan_email)}"))
            runCatching { startActivity(intent) }.onFailure {
                Toast.makeText(requireContext(), "No email app found on device.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSavedPhoto() {
        val savedPath = AppPreferences.getDeveloperPhotoPath(requireContext())
        if (!savedPath.isNullOrEmpty()) {
            val file = File(savedPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    binding.ivHassanProfile.setImageBitmap(bitmap)
                    return
                }
            }
        }
        binding.ivHassanProfile.setImageResource(R.drawable.img_hassan_profile)
    }

    private fun saveAndApplyPhoto(uri: Uri) {
        runCatching {
            val ctx = requireContext()
            val inputStream = ctx.contentResolver.openInputStream(uri) ?: return
            val destFile = File(ctx.filesDir, "hassan_custom_profile.jpg")
            FileOutputStream(destFile).use { out ->
                inputStream.copyTo(out)
            }
            AppPreferences.setDeveloperPhotoPath(ctx, destFile.absolutePath)
            val bitmap = BitmapFactory.decodeFile(destFile.absolutePath)
            if (bitmap != null) {
                binding.ivHassanProfile.setImageBitmap(bitmap)
                Toast.makeText(ctx, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            Toast.makeText(requireContext(), "Failed to save photo: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
