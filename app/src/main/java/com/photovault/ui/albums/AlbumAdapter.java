package com.photovault.ui.albums;

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
 * 2-column grid adapter for albums.
 */
public class AlbumAdapter extends ListAdapter<AlbumAdapter.AlbumItem, AlbumAdapter.ViewHolder> {

    private final Context context;
    private final OnAlbumClickListener listener;

    public AlbumAdapter(Context context, OnAlbumClickListener listener) {
        super(DIFF);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumItem item = getItem(position);
        holder.tvName.setText(item.name);
        holder.tvCount.setText(context.getString(R.string.photo_count, item.photoCount));
        if (item.accountEmail != null) {
            holder.tvAccount.setText(item.accountEmail);
            holder.tvAccount.setVisibility(View.VISIBLE);
        } else {
            holder.tvAccount.setVisibility(View.GONE);
        }
        if (item.routingTarget != null) {
            holder.tvRouting.setText("→ " + item.routingTarget);
            holder.tvRouting.setVisibility(View.VISIBLE);
        } else {
            holder.tvRouting.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount, tvAccount, tvRouting;
        ViewHolder(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvAlbumName);
            tvCount   = v.findViewById(R.id.tvAlbumCount);
            tvAccount = v.findViewById(R.id.tvAlbumAccount);
            tvRouting = v.findViewById(R.id.tvAlbumRouting);
        }
    }

    public static class AlbumItem {
        public String id;
        public String name;
        public int photoCount;
        public String coverPath;
        public String accountEmail;
        public String routingTarget;
        public boolean isLocal;
    }

    private static final DiffUtil.ItemCallback<AlbumItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlbumItem a, @NonNull AlbumItem b) {
            return a.id.equals(b.id);
        }
        @Override
        public boolean areContentsTheSame(@NonNull AlbumItem a, @NonNull AlbumItem b) {
            return a.photoCount == b.photoCount && a.name.equals(b.name);
        }
    };

    public interface OnAlbumClickListener {
        void onClick(AlbumItem item);
    }
}
