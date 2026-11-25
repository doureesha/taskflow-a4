package com.example.taskflow;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

// one card per project in the Home RecyclerView
public class ProjectAdapter extends ListAdapter<ProjectRow, ProjectAdapter.ProjectViewHolder> {

    public interface ClickListener {
        void onProjectClick(int projectId);
    }

    private static final String[] TAG_BG = {"#EEF0FF", "#FFF1E6", "#E8F3FF", "#E6F7F1", "#FDE8F1"};
    private static final String[] TAG_TEXT = {"#5B5BD6", "#E67700", "#2B7DE9", "#0F9D75", "#D6336C"};

    private ClickListener listener;

    public ProjectAdapter(ClickListener listener) {
        super(new DiffUtil.ItemCallback<ProjectRow>() {
            // same project = same id
            @Override
            public boolean areItemsTheSame(@NonNull ProjectRow oldItem, @NonNull ProjectRow newItem) {
                return oldItem.getId() == newItem.getId();
            }

            // same row on screen = every shown value is the same
            @Override
            public boolean areContentsTheSame(@NonNull ProjectRow oldItem, @NonNull ProjectRow newItem) {
                return oldItem.sameContent(newItem);
            }
        });
        this.listener = listener;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView txtTag, txtName, txtDescription, txtTasks, txtPercent, txtAttachments, txtComments, txtInvite;
        ProgressBar progress;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTag = itemView.findViewById(R.id.txtRowTag);
            txtName = itemView.findViewById(R.id.txtRowName);
            txtDescription = itemView.findViewById(R.id.txtRowDescription);
            txtTasks = itemView.findViewById(R.id.txtRowTasks);
            txtPercent = itemView.findViewById(R.id.txtRowPercent);
            progress = itemView.findViewById(R.id.progressRow);
            txtAttachments = itemView.findViewById(R.id.txtRowAttachments);
            txtComments = itemView.findViewById(R.id.txtRowComments);
            txtInvite = itemView.findViewById(R.id.txtRowInvite);
        }
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_row, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectRow row = getItem(position);

        holder.txtName.setText(row.getName());
        holder.txtDescription.setText(row.getDescription());
        holder.txtTasks.setText(row.getDone() + "/" + row.getTotal() + " tasks");
        holder.txtPercent.setText(row.getPercent() + "%");
        holder.progress.setProgress(row.getPercent());
        holder.txtAttachments.setText(row.getAttachmentCount() + " Attachments");
        holder.txtComments.setText(row.getCommentCount() + " Comments");

        // tag chip color depends on the tag text
        int colorIndex = Math.abs(row.getTag().hashCode()) % TAG_BG.length;
        GradientDrawable chip = new GradientDrawable();
        chip.setColor(Color.parseColor(TAG_BG[colorIndex]));
        chip.setCornerRadius(8f);
        holder.txtTag.setBackground(chip);
        holder.txtTag.setTextColor(Color.parseColor(TAG_TEXT[colorIndex]));
        holder.txtTag.setText(row.getTag());

        // the design only shows the invite pill on the first card, it does nothing
        if (row.isShowInvite()) {
            holder.txtInvite.setVisibility(View.VISIBLE);
        } else {
            holder.txtInvite.setVisibility(View.GONE);
        }

        // the project id travels to the next screen, never the row position
        int projectId = row.getId();
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(projectId));
    }
}
