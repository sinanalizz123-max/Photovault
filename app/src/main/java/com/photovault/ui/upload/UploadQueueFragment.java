package com.photovault.ui.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.photovault.R;
import com.photovault.databinding.FragmentUploadQueueBinding;
import com.photovault.database.entity.UploadTarget;
import com.photovault.viewmodel.UploadViewModel;

/**
 * Bottom sheet that shows the upload queue, per-account quota bars,
 * and allows drag-reorder.
 */
public class UploadQueueFragment extends Fragment {

    private FragmentUploadQueueBinding binding;
    private UploadViewModel viewModel;
    private UploadQueueAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(UploadViewModel.class);

        setupRecycler();
        setupButtons();
        observeData();
    }

    private void setupRecycler() {
        adapter = new UploadQueueAdapter(requireContext(),
                target -> viewModel.cancelTarget(target.id),
                target -> viewModel.retryTarget(target.id));

        binding.recyclerQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerQueue.setAdapter(adapter);

        // Drag-to-reorder
        ItemTouchHelper touchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                         @NonNull RecyclerView.ViewHolder from,
                                         @NonNull RecyclerView.ViewHolder to) {
                        int fromPos = from.getAdapterPosition();
                        int toPos   = to.getAdapterPosition();
                        adapter.moveItem(fromPos, toPos);
                        viewModel.reorderQueue(adapter.getCurrentList());
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {}
                });
        touchHelper.attachToRecyclerView(binding.recyclerQueue);
    }

    private void setupButtons() {
        binding.btnPauseAll.setOnClickListener(v -> viewModel.pauseAll());
        binding.btnResumeAll.setOnClickListener(v -> viewModel.resumeAll());
        binding.btnClose.setOnClickListener(v -> {
            // Close bottom sheet via parent activity
            requireActivity().getSupportFragmentManager()
                    .beginTransaction().remove(this).commit();
        });
    }

    private void observeData() {
        viewModel.getActiveQueue().observe(getViewLifecycleOwner(), targets -> {
            adapter.submitList(targets);

            // Stat counts
            int uploading = 0, waiting = 0, done = 0, failed = 0;
            if (targets != null) {
                for (UploadTarget t : targets) {
                    switch (t.status) {
                        case UploadTarget.STATUS_UPLOADING: uploading++; break;
                        case UploadTarget.STATUS_WAITING:  waiting++;   break;
                        case UploadTarget.STATUS_DONE:     done++;      break;
                        case UploadTarget.STATUS_FAILED:   failed++;    break;
                    }
                }
            }
            binding.tvUploading.setText(String.valueOf(uploading));
            binding.tvWaiting.setText(String.valueOf(waiting));
            binding.tvDone.setText(String.valueOf(done));
            binding.tvFailed.setText(String.valueOf(failed));

            String subtitle = uploading > 0
                    ? uploading + " uploading · " + waiting + " waiting"
                    : "Queue idle";
            binding.tvSubtitle.setText(subtitle);
        });

        // Quota bars
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            binding.quotaBarsContainer.removeAllViews();
            if (accounts == null) return;
            for (var account : accounts) {
                View bar = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_quota_bar, binding.quotaBarsContainer, false);
                ((android.widget.TextView) bar.findViewById(R.id.tvQuotaEmail))
                        .setText(account.email);
                ((android.widget.TextView) bar.findViewById(R.id.tvQuotaCount))
                        .setText(account.apiCallsToday + " / 9000");
                ((android.widget.ProgressBar) bar.findViewById(R.id.progressQuota))
                        .setProgress((int) (account.apiCallsToday / 90f)); // 0-100
                if (account.isPaused) {
                    bar.findViewById(R.id.tvQuotaPaused).setVisibility(View.VISIBLE);
                }
                binding.quotaBarsContainer.addView(bar);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
