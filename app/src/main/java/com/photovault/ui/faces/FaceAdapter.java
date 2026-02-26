package com.photovault.ui.faces;

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

/**
 * 4-column face cluster grid adapter.
 */
public class FaceAdapter extends ListAdapter<FaceAdapter.FaceItem, FaceAdapter.ViewHolder> {

    private final Context context;
    private final OnFaceClickListener listener;

    public FaceAdapter(Context context, OnFaceClickListener listener) {
        super(DIFF);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_face, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaceItem item = getItem(position);

        // Avatar initials or "?"
        holder.tvInitials.setText(item.isNamed ? item.initials : "?");
        holder.tvName.setText(item.isNamed ? item.name : "Unknown");
        holder.tvCount.setText(
                context.getString(R.string.photo_count_short, item.photoCount));

        // Low confidence indicator
        if (item.isLowConfidence) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText("Low conf.");
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }

        // Named border colour
        holder.vAvatarBorder.setSelected(item.isNamed);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vAvatarBorder;
        TextView tvInitials, tvName, tvCount, tvBadge;

        ViewHolder(View v) {
            super(v);
            vAvatarBorder = v.findViewById(R.id.vAvatarBorder);
            tvInitials    = v.findViewById(R.id.tvInitials);
            tvName        = v.findViewById(R.id.tvFaceName);
            tvCount       = v.findViewById(R.id.tvFaceCount);
            tvBadge       = v.findViewById(R.id.tvFaceBadge);
        }
    }

    public static class FaceItem {
        public long clusterId;
        public String name;
        public String initials;
        public int photoCount;
        public boolean isNamed;
        public boolean isLowConfidence;
        public String coverPhotoPath;
    }

    private static final DiffUtil.ItemCallback<FaceItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull FaceItem a, @NonNull FaceItem b) {
            return a.clusterId == b.clusterId;
        }
        @Override
        public boolean areContentsTheSame(@NonNull FaceItem a, @NonNull FaceItem b) {
            return a.photoCount == b.photoCount && a.isNamed == b.isNamed;
        }
    };

    public interface OnFaceClickListener {
        void onClick(FaceItem item);
    }
}
