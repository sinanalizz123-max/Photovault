package com.photovault.ui.albums

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.photovault.databinding.FragmentAlbumsBinding
import com.photovault.ui.rules.RulesActivity

class AlbumsFragment : Fragment() {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.btnLocal.setOnClickListener { switchMode(false) }
        binding.btnCloud.setOnClickListener { switchMode(true) }
        binding.fabAddAlbum.setOnClickListener {
            startActivity(Intent(requireContext(), RulesActivity::class.java))
        }
    }

    private fun switchMode(cloud: Boolean) {
        binding.scrollAccountFilters.visibility = if (cloud) View.VISIBLE else View.GONE
        // TODO: swap album data source
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
