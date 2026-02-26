package com.photovault.ui.faces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.photovault.databinding.FragmentFacesBinding

class FacesFragment : Fragment() {

    private var _binding: FragmentFacesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentFacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFaces.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.btnLocal.setOnClickListener { switchMode(false) }
        binding.btnCloud.setOnClickListener { switchMode(true) }
        // TODO: attach FaceAdapter backed by Room face_clusters
    }

    private fun switchMode(cloud: Boolean) {
        binding.tvPrivacyInfo.visibility = if (!cloud) View.VISIBLE else View.GONE
        // TODO: switch face data source
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
