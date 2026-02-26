package com.photovault.ui.rules;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.photovault.R;
import com.photovault.database.entity.Rule;
import com.photovault.databinding.DialogAddEditRuleBinding;
import com.photovault.viewmodel.PhotosViewModel;

import java.util.Arrays;
import java.util.List;

/**
 * Bottom sheet for creating or editing a routing rule.
 */
public class AddEditRuleDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_RULE_ID = "rule_id";

    private DialogAddEditRuleBinding binding;
    private PhotosViewModel viewModel;
    private long ruleId = -1;
    private Rule existingRule;

    public static AddEditRuleDialogFragment newInstance(long ruleId) {
        AddEditRuleDialogFragment f = new AddEditRuleDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RULE_ID, ruleId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddEditRuleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PhotosViewModel.class);

        if (getArguments() != null) {
            ruleId = getArguments().getLong(ARG_RULE_ID, -1);
        }

        setupRuleTypeSpinner();
        setupAccountSpinner();

        if (ruleId != -1) {
            loadExistingRule();
        }

        binding.btnSave.setOnClickListener(v -> saveRule());
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupRuleTypeSpinner() {
        List<String> types = Arrays.asList(
                "Manual Tag", "Face", "Folder", "Media Type",
                "Device", "File Size", "Resolution", "Date",
                "App Source", "Location", "Default");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRuleType.setAdapter(adapter);

        binding.spinnerRuleType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                        updateConditionHint(pos);
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> p) {}
                });
    }

    private void updateConditionHint(int typeIndex) {
        String[] hints = {
                "Tag name e.g. Work",
                "Face cluster ID",
                "Folder path e.g. DCIM/Camera",
                "image / video / gif / raw",
                "Device model name",
                "Size threshold in MB",
                "Resolution e.g. 3840",
                "Date e.g. 2024-01-01",
                "App signature path",
                "lat,lng,radiusM",
                "(leave empty)"
        };
        binding.etConditionValue.setHint(
                typeIndex < hints.length ? hints[typeIndex] : "Condition");
    }

    private void setupAccountSpinner() {
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts == null) return;
            String[] emails = accounts.stream()
                    .map(a -> a.email)
                    .toArray(String[]::new);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_spinner_item, emails);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerAccount.setAdapter(adapter);
        });
    }

    private void loadExistingRule() {
        // Load from DB via viewModel
        viewModel.getRuleById(ruleId).observe(getViewLifecycleOwner(), rule -> {
            if (rule == null) return;
            existingRule = rule;
            binding.etRuleName.setText(rule.ruleName);
            binding.etConditionValue.setText(rule.conditionValue);
            binding.etAlbumName.setText(rule.albumName);
            binding.etReplicateTo.setText(rule.replicateToAccountIds);
            binding.switchEnabled.setChecked(rule.isEnabled);
            binding.tvTitle.setText(R.string.edit_rule);
        });
    }

    private void saveRule() {
        String name      = binding.etRuleName.getText().toString().trim();
        String condition = binding.etConditionValue.getText().toString().trim();
        String album     = binding.etAlbumName.getText().toString().trim();
        String replicate = binding.etReplicateTo.getText().toString().trim();
        int typePos      = binding.spinnerRuleType.getSelectedItemPosition();
        boolean enabled  = binding.switchEnabled.isChecked();

        if (name.isEmpty()) {
            binding.etRuleName.setError(getString(R.string.field_required));
            return;
        }
        if (album.isEmpty()) {
            binding.etAlbumName.setError(getString(R.string.field_required));
            return;
        }

        Rule rule = existingRule != null ? existingRule : new Rule();
        rule.ruleName              = name;
        rule.ruleType              = typePos;
        rule.conditionValue        = condition;
        rule.albumName             = album;
        rule.replicateToAccountIds = replicate;
        rule.isEnabled             = enabled;
        rule.createdAt             = System.currentTimeMillis();

        if (existingRule != null) {
            viewModel.updateRule(rule);
        } else {
            viewModel.insertRule(rule);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
