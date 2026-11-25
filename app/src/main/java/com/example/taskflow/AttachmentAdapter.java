package com.example.taskflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

// one row per attached file in the Files RecyclerView
public class AttachmentAdapter extends ListAdapter<AttachmentRow, AttachmentAdapter.AttachmentViewHolder> {

    public interface RowListener {
        void onPreview(AttachmentRow row);
        void onRemove(AttachmentRow row);
    }

    private RowListener listener;

    public AttachmentAdapter(RowListener listener) {
        super(new DiffUtil.ItemCallback<AttachmentRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull AttachmentRow oldItem, @NonNull AttachmentRow newItem) {
                return oldItem.getLinkId() == newItem.getLinkId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull AttachmentRow oldItem, @NonNull AttachmentRow newItem) {
                return oldItem.sameContent(newItem);
            }
        });
        this.listener = listener;
    }

    public static class AttachmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon, btnRemove;
        TextView txtName, txtMeta, btnPreview;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgAttachmentIcon);
            txtName = itemView.findViewById(R.id.txtAttachmentName);
            txtMeta = itemView.findViewById(R.id.txtAttachmentMeta);
            btnPreview = itemView.findViewById(R.id.btnPreviewAttachment);
            btnRemove = itemView.findViewById(R.id.btnRemoveAttachment);
        }
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attachment_row, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        AttachmentRow row = getItem(position);

        holder.txtName.setText(row.getName());
        holder.txtMeta.setText(row.getMeta());

        if (row.isImage()) {
            holder.imgIcon.setImageResource(R.drawable.ic_image);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_file);
        }

        holder.btnPreview.setOnClickListener(v -> listener.onPreview(row));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(row));
    }
}
