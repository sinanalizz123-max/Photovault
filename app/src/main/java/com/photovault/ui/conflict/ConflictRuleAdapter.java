package com.photovault.ui.conflict;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.photovault.R;
import com.photovault.database.entity.Rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows matched rules in the conflict dialog with checkboxes.
 */
public class ConflictRuleAdapter
        extends RecyclerView.Adapter<ConflictRuleAdapter.ViewHolder> {

    private final Context context;
    private final OnSelectionChangedListener listener;
    private List<Rule> rules = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();

    public ConflictRuleAdapter(Context context, OnSelectionChangedListener listener) {
        this.context  = context;
        this.listener = listener;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
        // Pre-select all
        for (Rule r : rules) selectedIds.add(r.id);
        notifyDataSetChanged();
        listener.onChanged(selectedIds.size());
    }

    public Set<Long> getSelectedRuleIds() {
        return new HashSet<>(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_conflict_rule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rule rule = rules.get(position);
        holder.tvEmoji.setText(rule.ruleTypeEmoji());
        holder.tvName.setText(rule.ruleName);
        holder.tvDest.setText("→ " + rule.albumName + " · Priority #" + (position + 1));
        holder.tvType.setText(rule.ruleTypeLabel());

        holder.checkBox.setChecked(selectedIds.contains(rule.id));
        holder.checkBox.setOnCheckedChangeListener((v, checked) -> {
            if (checked) selectedIds.add(rule.id);
            else         selectedIds.remove(rule.id);
            listener.onChanged(selectedIds.size());
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDest, tvType;
        CheckBox checkBox;

        ViewHolder(View v) {
            super(v);
            tvEmoji  = v.findViewById(R.id.tvConflictEmoji);
            tvName   = v.findViewById(R.id.tvConflictRuleName);
            tvDest   = v.findViewById(R.id.tvConflictDest);
            tvType   = v.findViewById(R.id.tvConflictType);
            checkBox = v.findViewById(R.id.checkConflict);
        }
    }

    public interface OnSelectionChangedListener {
        void onChanged(int selectedCount);
    }
}
