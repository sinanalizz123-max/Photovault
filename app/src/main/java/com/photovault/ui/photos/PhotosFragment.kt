package com.photovault.ui.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.photovault.databinding.FragmentPhotosBinding

class PhotosFragment : Fragment() {

    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private var isCloudMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToggle()
        setupGrid()
    }

    private fun setupToggle() {
        binding.btnLocal.setOnClickListener { switchMode(false) }
        binding.btnCloud.setOnClickListener { switchMode(true) }
    }

    private fun switchMode(cloud: Boolean) {
        isCloudMode = cloud
        binding.btnLocal.isSelected = !cloud
        binding.btnCloud.isSelected = cloud
        binding.scrollFilters.visibility = if (cloud) View.VISIBLE else View.GONE
        binding.tvCloudInfo.visibility = if (cloud) View.VISIBLE else View.GONE
        // TODO: swap adapter data source (MediaStore vs local DB)
    }

    private fun setupGrid() {
        binding.rvPhotos.layoutManager = GridLayoutManager(requireContext(), 3)
        // TODO: Attach PhotosAdapter with MediaStore paging source
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
