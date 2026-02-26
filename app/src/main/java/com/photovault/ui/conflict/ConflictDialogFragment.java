package com.photovault.ui.conflict;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.photovault.R;
import com.photovault.databinding.DialogConflictBinding;
import com.photovault.viewmodel.UploadViewModel;

/**
 * Bottom-sheet conflict resolution dialog.
 * Shows which rules matched, lets user select destinations,
 * and offers: upload to selected / follow priority / always priority / cancel.
 */
public class ConflictDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_MEDIA_ID = "media_id";

    private DialogConflictBinding binding;
    private UploadViewModel viewModel;
    private long mediaId;
    private ConflictRuleAdapter adapter;

    public static ConflictDialogFragment newInstance(long mediaId) {
        ConflictDialogFragment f = new ConflictDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MEDIA_ID, mediaId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogConflictBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(UploadViewModel.class);

        if (getArguments() != null) {
            mediaId = getArguments().getLong(ARG_MEDIA_ID);
        }

        setupRecycler();
        setupButtons();
        loadConflict();
    }

    private void setupRecycler() {
        adapter = new ConflictRuleAdapter(requireContext(), selected -> {
            // Update button label with selection count
            int count = selected;
            binding.btnUploadSelected.setText(
                    count > 0
                    ? getString(R.string.upload_to_selected, count)
                    : getString(R.string.upload_to_selected, 0));
        });
        binding.recyclerConflictRules.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerConflictRules.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnUploadSelected.setOnClickListener(v -> {
            viewModel.resolveConflict(mediaId,
                    adapter.getSelectedRuleIds(),
                    UploadViewModel.ConflictResolution.SELECTED);
            dismiss();
        });

        binding.btnFollowPriority.setOnClickListener(v -> {
            viewModel.resolveConflict(mediaId,
                    null,
                    UploadViewModel.ConflictResolution.PRIORITY);
            dismiss();
        });

        binding.btnAlwaysPriority.setOnClickListener(v -> {
            viewModel.setAlwaysFollowPriority(true);
            viewModel.resolveConflict(mediaId,
                    null,
                    UploadViewModel.ConflictResolution.PRIORITY);
            dismiss();
        });

        binding.btnCancelUpload.setOnClickListener(v -> {
            viewModel.cancelMediaUpload(mediaId);
            dismiss();
        });
    }

    private void loadConflict() {
        viewModel.getConflictRules(mediaId).observe(getViewLifecycleOwner(), rules -> {
            if (rules == null || rules.isEmpty()) {
                dismiss();
                return;
            }
            adapter.setRules(rules);
            binding.tvConflictCount.setText(
                    getString(R.string.conflict_count, rules.size()));
        });

        viewModel.getMediaInfo(mediaId).observe(getViewLifecycleOwner(), info -> {
            if (info == null) return;
            binding.tvFilename.setText(info.filename);
            binding.tvFilesize.setText(info.sizeLabel);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
