package com.shopkeeper.mobileshop.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun openUrl(url: String) {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }.onFailure {
                Toast.makeText(requireContext(), "Could not open link", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWhatsApp.setOnClickListener { openUrl(getString(R.string.url_whatsapp)) }
        binding.btnFacebook.setOnClickListener { openUrl(getString(R.string.url_facebook)) }
        binding.btnInstagram.setOnClickListener { openUrl(getString(R.string.url_instagram)) }
        binding.btnYouTube.setOnClickListener { openUrl(getString(R.string.url_youtube)) }
        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.hassan_email)}"))
            runCatching { startActivity(intent) }.onFailure {
                Toast.makeText(requireContext(), getString(R.string.hassan_email), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
