package com.photovault.ui.upload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.photovault.R;
import com.photovault.database.entity.UploadTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Upload queue item adapter supporting drag-reorder.
 */
public class UploadQueueAdapter
        extends ListAdapter<UploadTarget, UploadQueueAdapter.ViewHolder> {

    private final Context context;
    private final OnCancelListener cancelListener;
    private final OnRetryListener retryListener;

    public UploadQueueAdapter(Context context,
                              OnCancelListener cancelListener,
                              OnRetryListener retryListener) {
        super(DIFF);
        this.context        = context;
        this.cancelListener = cancelListener;
        this.retryListener  = retryListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_upload_queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadTarget item = getItem(position);

        holder.tvFilename.setText(filenameFrom(item));
        holder.tvDestination.setText(item.albumName != null ? "→ " + item.albumName : "→ Uncategorized");
        holder.tvStatus.setText(item.statusLabel());
        holder.tvPercent.setText(item.progressPercent + "%");
        holder.progressBar.setProgress(item.progressPercent);

        // Show/hide actions
        boolean failed = item.status == UploadTarget.STATUS_FAILED;
        holder.btnCancel.setVisibility(failed ? View.GONE : View.VISIBLE);
        holder.btnRetry.setVisibility(failed ? View.VISIBLE : View.GONE);

        if (item.errorMessage != null && !item.errorMessage.isEmpty()) {
            holder.tvError.setText(item.errorMessage);
            holder.tvError.setVisibility(View.VISIBLE);
        } else {
            holder.tvError.setVisibility(View.GONE);
        }

        holder.btnCancel.setOnClickListener(v -> cancelListener.onCancel(item));
        holder.btnRetry.setOnClickListener(v -> retryListener.onRetry(item));
    }

    // ── Drag-reorder support ─────────────────────────────────────────────────

    public void moveItem(int from, int to) {
        List<UploadTarget> list = new ArrayList<>(getCurrentList());
        UploadTarget moved = list.remove(from);
        list.add(to, moved);
        submitList(list);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String filenameFrom(UploadTarget target) {
        // We don't have the path here; in production you'd join with media_items
        return "media_" + target.mediaId + ".jpg";
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView   tvFilename, tvDestination, tvStatus, tvPercent, tvError;
        ProgressBar progressBar;
        View       btnCancel, btnRetry;

        ViewHolder(View v) {
            super(v);
            tvFilename    = v.findViewById(R.id.tvUploadFilename);
            tvDestination = v.findViewById(R.id.tvUploadDestination);
            tvStatus      = v.findViewById(R.id.tvUploadStatus);
            tvPercent     = v.findViewById(R.id.tvUploadPercent);
            tvError       = v.findViewById(R.id.tvUploadError);
            progressBar   = v.findViewById(R.id.progressUpload);
            btnCancel     = v.findViewById(R.id.btnCancelUpload);
            btnRetry      = v.findViewById(R.id.btnRetryUpload);
        }
    }

    private static final DiffUtil.ItemCallback<UploadTarget> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull UploadTarget a, @NonNull UploadTarget b) {
                    return a.id == b.id;
                }
                @Override
                public boolean areContentsTheSame(@NonNull UploadTarget a, @NonNull UploadTarget b) {
                    return a.status == b.status && a.progressPercent == b.progressPercent;
                }
            };

    public interface OnCancelListener { void onCancel(UploadTarget target); }
    public interface OnRetryListener  { void onRetry(UploadTarget target);  }
}
