package com.example.taskflow;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// ONE adapter for the whole grouped task list. getItemViewType tells it
// if a position is a section header or a task row
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HEADER = 0;
    private static final int VIEW_TASK = 1;

    public interface RowListener {
        void onChecked(int taskId, boolean checked);
        void onRowClick(int taskId);
    }

    private static final String[] STATUS_LABELS = {"To do", "In progress", "Done"};
    private static final String[] STATUS_COLORS = {"#5B6478", "#2B7DE9", "#22A06B"};
    private static final String[] PRIORITY_LABELS = {"High", "Normal", "Low"};
    private static final String[] PRIORITY_COLORS = {"#E5484D", "#5B6478", "#8A93A5"};
    // header colors in group order: Overdue, In progress, To do, Done
    private static final String[] HEADER_COLORS = {"#E5484D", "#22A06B", "#2B7DE9", "#22A06B"};

    private ArrayList<TaskRow> rows = new ArrayList<>();
    private RowListener listener;

    public TaskAdapter(RowListener listener) {
        this.listener = listener;
    }

    public ArrayList<TaskRow> getRows() {
        return rows;
    }

    public TaskRow getRow(int position) {
        return rows.get(position);
    }

    // swaps in a new list. DiffUtil works out the small changes, so no notifyDataSetChanged.
    // detectMoves is false so a task that changes group is shown as remove + insert
    public void setRows(ArrayList<TaskRow> newRows) {
        final ArrayList<TaskRow> oldRows = rows;
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldRows.size();
            }

            @Override
            public int getNewListSize() {
                return newRows.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return oldRows.get(oldPos).sameItem(newRows.get(newPos));
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return oldRows.get(oldPos).sameContent(newRows.get(newPos));
            }
        }, false);

        rows = newRows;
        result.dispatchUpdatesTo(this);
    }

    // used while dragging: move one row and tell the RecyclerView
    public void moveRow(int from, int to) {
        TaskRow row = rows.remove(from);
        rows.add(to, row);
        notifyItemMoved(from, to);
    }

    @Override
    public int getItemViewType(int position) {
        if (rows.get(position).isHeader()) {
            return VIEW_HEADER;
        }
        return VIEW_TASK;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtCount;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtHeaderTitle);
            txtCount = itemView.findViewById(R.id.txtHeaderCount);
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox chkTask;
        TextView txtDescription, txtStatus, txtPriority, txtDue, txtOverdue;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            chkTask = itemView.findViewById(R.id.chkTask);
            txtDescription = itemView.findViewById(R.id.txtTaskRowDescription);
            txtStatus = itemView.findViewById(R.id.txtTaskRowStatus);
            txtPriority = itemView.findViewById(R.id.txtTaskRowPriority);
            txtDue = itemView.findViewById(R.id.txtTaskRowDue);
            txtOverdue = itemView.findViewById(R.id.txtTaskRowOverdue);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_task_header, parent, false));
        }
        return new TaskViewHolder(inflater.inflate(R.layout.item_task_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskRow row = rows.get(position);

        if (row.isHeader()) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.txtTitle.setText(row.getHeaderTitle());
            headerHolder.txtTitle.setTextColor(Color.parseColor(HEADER_COLORS[row.getGroup()]));
            headerHolder.txtCount.setText(String.valueOf(row.getHeaderCount()));
            headerHolder.txtCount.setTextColor(Color.parseColor(HEADER_COLORS[row.getGroup()]));
            return;
        }

        TaskViewHolder taskHolder = (TaskViewHolder) holder;
        int taskId = row.getTaskId();

        taskHolder.chkTask.setChecked(row.isDone());
        taskHolder.chkTask.setOnClickListener(v -> listener.onChecked(taskId, taskHolder.chkTask.isChecked()));
        taskHolder.itemView.setOnClickListener(v -> listener.onRowClick(taskId));

        taskHolder.txtDescription.setText(row.getDescription());
        if (row.isDone()) {
            taskHolder.txtDescription.setPaintFlags(taskHolder.txtDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskHolder.txtDescription.setTextColor(Color.parseColor("#6B7385"));
        } else {
            taskHolder.txtDescription.setPaintFlags(taskHolder.txtDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            taskHolder.txtDescription.setTextColor(Color.parseColor("#141B34"));
        }

        taskHolder.txtStatus.setText(STATUS_LABELS[row.getStatusIndex()]);
        taskHolder.txtStatus.setTextColor(Color.parseColor(STATUS_COLORS[row.getStatusIndex()]));

        taskHolder.txtPriority.setText(PRIORITY_LABELS[row.getPriorityIndex()]);
        taskHolder.txtPriority.setTextColor(Color.parseColor(PRIORITY_COLORS[row.getPriorityIndex()]));

        taskHolder.txtDue.setText(row.getDueText());

        if (row.isOverdue()) {
            taskHolder.txtOverdue.setVisibility(View.VISIBLE);
        } else {
            taskHolder.txtOverdue.setVisibility(View.GONE);
        }
    }
}
