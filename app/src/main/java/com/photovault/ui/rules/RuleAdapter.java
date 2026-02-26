package com.photovault.ui.rules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.photovault.R;
import com.photovault.database.entity.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays routing rules with priority badge, type emoji, condition, and target chips.
 * Supports drag-reorder via public moveItem().
 */
public class RuleAdapter extends ListAdapter<Rule, RuleAdapter.ViewHolder> {

    private final Context context;
    private final OnRuleClickListener editListener;
    private final OnRuleClickListener deleteListener;

    public RuleAdapter(Context context,
                       OnRuleClickListener editListener,
                       OnRuleClickListener deleteListener) {
        super(DIFF);
        this.context        = context;
        this.editListener   = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_rule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rule rule = getItem(position);

        holder.tvEmoji.setText(rule.ruleTypeEmoji());
        holder.tvName.setText(rule.ruleName);
        holder.tvCondition.setText(rule.conditionValue);
        holder.tvPriority.setText(context.getString(R.string.priority_label, position + 1));
        holder.tvAlbum.setText(rule.albumName != null ? rule.albumName : "—");

        // Replication chip
        holder.chipReplicate.setVisibility(
                rule.hasReplication() ? View.VISIBLE : View.GONE);

        // Enabled switch
        holder.switchEnabled.setChecked(rule.isEnabled);

        holder.itemView.setOnClickListener(v -> editListener.onClick(rule));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(rule));
    }

    public void moveItem(int from, int to) {
        List<Rule> list = new ArrayList<>(getCurrentList());
        Rule moved = list.remove(from);
        list.add(to, moved);
        submitList(list);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvCondition, tvPriority, tvAlbum;
        View chipReplicate, btnDelete;
        android.widget.Switch switchEnabled;

        ViewHolder(View v) {
            super(v);
            tvEmoji       = v.findViewById(R.id.tvRuleEmoji);
            tvName        = v.findViewById(R.id.tvRuleName);
            tvCondition   = v.findViewById(R.id.tvRuleCondition);
            tvPriority    = v.findViewById(R.id.tvRulePriority);
            tvAlbum       = v.findViewById(R.id.tvRuleAlbum);
            chipReplicate = v.findViewById(R.id.chipReplicate);
            btnDelete     = v.findViewById(R.id.btnDeleteRule);
            switchEnabled = v.findViewById(R.id.switchRuleEnabled);
        }
    }

    private static final DiffUtil.ItemCallback<Rule> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Rule a, @NonNull Rule b) {
            return a.id == b.id;
        }
        @Override
        public boolean areContentsTheSame(@NonNull Rule a, @NonNull Rule b) {
            return a.priorityOrder == b.priorityOrder
                    && a.isEnabled == b.isEnabled
                    && a.albumName != null && a.albumName.equals(b.albumName);
        }
    };

    public interface OnRuleClickListener {
        void onClick(Rule rule);
    }
}
