package com.photovault.ui.conflict;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.photovault.R;
import com.photovault.databinding.DialogFaceDetailBinding;
import com.photovault.ui.photos.PhotoAdapter;
import com.photovault.viewmodel.PhotosViewModel;

/**
 * Bottom sheet showing face cluster details — name, routing destination,
 * sample photos, and actions (edit routing / rename / merge / remove).
 */
public class FaceDetailDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_CLUSTER_ID = "cluster_id";

    private DialogFaceDetailBinding binding;
    private PhotosViewModel viewModel;
    private long clusterId;

    public static FaceDetailDialogFragment newInstance(long clusterId) {
        FaceDetailDialogFragment f = new FaceDetailDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CLUSTER_ID, clusterId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogFaceDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PhotosViewModel.class);

        if (getArguments() != null) {
            clusterId = getArguments().getLong(ARG_CLUSTER_ID);
        }

        setupSamplePhotos();
        loadCluster();
        setupButtons();
    }

    private void setupSamplePhotos() {
        PhotoAdapter sampleAdapter = new PhotoAdapter(requireContext(), item -> {});
        binding.recyclerSamplePhotos.setLayoutManager(
                new GridLayoutManager(requireContext(), 4));
        binding.recyclerSamplePhotos.setAdapter(sampleAdapter);
        viewModel.getPhotosByCluster(clusterId).observe(getViewLifecycleOwner(),
                photos -> {
                    // Convert MediaItem list to PhotoItem list (sample only)
                    // In production you'd transform via a mapper
                    // sampleAdapter.submitList(converted);
                });
    }

    private void loadCluster() {
        viewModel.getCluster(clusterId).observe(getViewLifecycleOwner(), cluster -> {
            if (cluster == null) { dismiss(); return; }

            binding.tvInitials.setText(cluster.initials());
            binding.tvPersonName.setText(cluster.displayName());
            binding.tvPhotoCount.setText(
                    getString(R.string.photo_count, cluster.photoCount));

            if (cluster.isLowConfidence()) {
                binding.tvLowConfidence.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getRoutingRuleForCluster(clusterId).observe(getViewLifecycleOwner(), rule -> {
            if (rule == null) {
                binding.tvRouteAccount.setText(R.string.no_rule);
                binding.tvRouteAlbum.setText(R.string.no_rule);
            } else {
                binding.tvRouteAccount.setText(rule.albumName); // simplified
                binding.tvRouteAlbum.setText(rule.albumName);
                binding.tvRoutePriority.setText(
                        getString(R.string.priority_label, rule.priorityOrder + 1));
            }
        });
    }

    private void setupButtons() {
        binding.btnEditRouting.setOnClickListener(v ->
                com.photovault.ui.rules.AddEditRuleDialogFragment.newInstance(-1)
                        .show(getChildFragmentManager(), "edit_rule"));

        binding.btnRename.setOnClickListener(v -> showRenameDialog());

        binding.btnMerge.setOnClickListener(v ->
                android.widget.Toast.makeText(requireContext(),
                        R.string.merge_coming_soon, android.widget.Toast.LENGTH_SHORT).show());

        binding.btnRemoveCluster.setOnClickListener(v -> confirmRemoveCluster());
    }

    private void showRenameDialog() {
        android.widget.EditText et = new android.widget.EditText(requireContext());
        et.setHint(R.string.person_name_hint);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename_person)
                .setView(et)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.renameCluster(clusterId, name);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmRemoveCluster() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.remove_cluster_title)
                .setMessage(R.string.remove_cluster_message)
                .setPositiveButton(R.string.remove, (d, w) -> {
                    viewModel.deleteCluster(clusterId);
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
