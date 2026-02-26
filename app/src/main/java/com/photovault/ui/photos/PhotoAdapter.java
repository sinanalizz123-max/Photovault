package com.photovault.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.photovault.R;
import com.photovault.database.entity.UploadTarget;

import java.io.File;

/**
 * Grid adapter for the Photos tab.
 * Displays status badge: Uploaded / Pending / Failed / Local only / Conflict.
 */
public class PhotoAdapter extends ListAdapter<PhotoAdapter.PhotoItem, PhotoAdapter.ViewHolder> {

    private final Context context;
    private final OnPhotoClickListener listener;

    public PhotoAdapter(Context context, OnPhotoClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoItem item = getItem(position);

        Glide.with(context)
                .load(new File(item.localPath))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .placeholder(R.drawable.bg_photo_placeholder)
                .into(holder.imgThumb);

        // Status badge
        String badgeText;
        int badgeColorRes;
        switch (item.uploadStatus) {
            case UploadTarget.STATUS_DONE:
                badgeText = "✓ Uploaded";
                badgeColorRes = R.color.status_uploaded;
                break;
            case UploadTarget.STATUS_UPLOADING:
                badgeText = "↑ Uploading";
                badgeColorRes = R.color.status_uploading;
                break;
            case UploadTarget.STATUS_FAILED:
                badgeText = "✗ Failed";
                badgeColorRes = R.color.status_failed;
                break;
            case -2:   // conflict sentinel
                badgeText = "⚠ Conflict";
                badgeColorRes = R.color.status_conflict;
                break;
            case -1:   // no target → local only
                badgeText = "Local only";
                badgeColorRes = R.color.status_local;
                break;
            default:   // waiting / paused
                badgeText = "⏳ Pending";
                badgeColorRes = R.color.status_pending;
        }

        holder.tvBadge.setText(badgeText);
        holder.tvBadge.setBackgroundTintList(
                ContextCompat.getColorStateList(context, badgeColorRes));

        // Video indicator
        holder.imgVideoIcon.setVisibility(item.isVideo ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb, imgVideoIcon;
        TextView tvBadge;

        ViewHolder(View v) {
            super(v);
            imgThumb    = v.findViewById(R.id.imgThumb);
            imgVideoIcon = v.findViewById(R.id.imgVideoIcon);
            tvBadge     = v.findViewById(R.id.tvBadge);
        }
    }

    // ── Data model ────────────────────────────────────────────────────────────

    public static class PhotoItem {
        public long mediaId;
        public String localPath;
        public int uploadStatus;    // UploadTarget.STATUS_* or -1 (local only) or -2 (conflict)
        public boolean isVideo;
        public boolean hasConflict;
        public String accountEmail; // cloud mode only
    }

    // ── DiffUtil ─────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<PhotoItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull PhotoItem a, @NonNull PhotoItem b) {
                    return a.mediaId == b.mediaId;
                }

                @Override
                public boolean areContentsTheSame(@NonNull PhotoItem a, @NonNull PhotoItem b) {
                    return a.uploadStatus == b.uploadStatus
                            && a.hasConflict == b.hasConflict;
                }
            };

    // ── Listener ─────────────────────────────────────────────────────────────

    public interface OnPhotoClickListener {
        void onClick(PhotoItem item);
    }
}
