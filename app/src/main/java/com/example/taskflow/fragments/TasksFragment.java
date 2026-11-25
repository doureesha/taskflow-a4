package com.example.taskflow.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskflow.AppData;
import com.example.taskflow.DialogHelper;
import com.example.taskflow.Project;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.R;
import com.example.taskflow.Task;
import com.example.taskflow.TaskAdapter;
import com.example.taskflow.TaskRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class TasksFragment extends Fragment {

    private static final String[] GROUP_NAMES = {"Overdue", "In progress", "To do", "Done"};
    private static final int SORT_MANUAL = 4;

    private int projectId;
    private ProjectHost host;

    // the current query. kept in fields so it survives while the user switches tabs
    private String query = "";
    private int statusPos = 0;
    private int priorityPos = 0;
    private int datePos = 0;
    private int sortPos = 0;
    private boolean syncing = false;

    private EditText edtSearch;
    private Spinner spnStatus, spnPriority, spnDate, spnSort;
    private TextView txtShowing, txtReset;
    private View layoutNoTasks, layoutNoMatch;
    private Button btnAddTask, btnAddTaskEmpty, btnResetEmpty;
    private RecyclerView recyclerTasks;

    private TaskAdapter adapter;

    public static TasksFragment newInstance(int projectId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt("PROJECT_ID", projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectId = getArguments().getInt("PROJECT_ID");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtSearch = view.findViewById(R.id.edtSearchTasks);
        spnStatus = view.findViewById(R.id.spnFilterStatus);
        spnPriority = view.findViewById(R.id.spnFilterPriority);
        spnDate = view.findViewById(R.id.spnFilterDate);
        spnSort = view.findViewById(R.id.spnSort);
        txtShowing = view.findViewById(R.id.txtShowingTasks);
        txtReset = view.findViewById(R.id.txtResetFilters);
        layoutNoTasks = view.findViewById(R.id.layoutNoTasks);
        layoutNoMatch = view.findViewById(R.id.layoutNoMatch);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        btnAddTaskEmpty = view.findViewById(R.id.btnAddTaskEmpty);
        btnResetEmpty = view.findViewById(R.id.btnResetEmpty);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);

        setupSpinner(spnStatus, new String[]{"All statuses", "To do", "In progress", "Done"}, 0);
        setupSpinner(spnPriority, new String[]{"All priorities", "High", "Normal", "Low"}, 1);
        setupSpinner(spnDate, new String[]{"All dates", "Today", "Overdue", "No due date"}, 2);
        setupSpinner(spnSort, new String[]{"Due date (earliest)", "Due date (latest)", "Priority", "Newest first", "Manual order"}, 3);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (syncing) return;
                query = s.toString();
                refresh();
            }
        });

        adapter = new TaskAdapter(new TaskAdapter.RowListener() {
            @Override
            public void onChecked(int taskId, boolean checked) {
                Task task = getProject().findTaskById(taskId);
                if (task == null) return;
                // ticking the checkbox sets Done, unticking puts it back to To do
                if (checked) {
                    task.setStatus(Task.Status.DONE);
                } else {
                    task.setStatus(Task.Status.TODO);
                }
                host.onProjectChanged();
                refresh();
            }

            @Override
            public void onRowClick(int taskId) {
                Task task = getProject().findTaskById(taskId);
                if (task != null) showTaskDialog(task);
            }
        });
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTasks.setAdapter(adapter);

        // swipe + drag for the task rows
        new ItemTouchHelper(new TaskTouchCallback()).attachToRecyclerView(recyclerTasks);

        txtReset.setOnClickListener(v -> resetFilters());
        btnResetEmpty.setOnClickListener(v -> resetFilters());
        btnAddTask.setOnClickListener(v -> showTaskDialog(null));
        btnAddTaskEmpty.setOnClickListener(v -> showTaskDialog(null));

        syncWidgets();
    }

    private void setupSpinner(Spinner spinner, String[] items, int which) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) return;
                if (which == 0) statusPos = position;
                else if (which == 1) priorityPos = position;
                else if (which == 2) datePos = position;
                else sortPos = position;
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // put the saved query back into the widgets
    private void syncWidgets() {
        syncing = true;
        edtSearch.setText(query);
        spnStatus.setSelection(statusPos);
        spnPriority.setSelection(priorityPos);
        spnDate.setSelection(datePos);
        spnSort.setSelection(sortPos);
        syncing = false;
    }

    private void resetFilters() {
        query = "";
        statusPos = 0;
        priorityPos = 0;
        datePos = 0;
        sortPos = 0;
        syncWidgets();
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Overview may have asked for a filter (To do, In progress, Done or Overdue)
        ProjectHost.FilterRequest request = host.takePendingFilter();
        if (request != null) {
            query = "";
            statusPos = 0;
            priorityPos = 0;
            datePos = 0;
            sortPos = 0;
            if (request.status != null) {
                statusPos = request.status.ordinal() + 1;
            }
            if (request.overdue) {
                datePos = 2;
            }
            syncWidgets();
        }

        refresh();
    }

    private Project getProject() {
        return AppData.findProjectById(projectId);
    }

    // which group a task lives in. overdue is checked first, so an unfinished past-due task is always in Overdue
    private int groupOf(Task task) {
        if (task.isOverdue()) return TaskRow.GROUP_OVERDUE;
        if (task.getStatus() == Task.Status.IN_PROGRESS) return TaskRow.GROUP_IN_PROGRESS;
        if (task.getStatus() == Task.Status.TODO) return TaskRow.GROUP_TODO;
        return TaskRow.GROUP_DONE;
    }

    // search + status + priority + date all have to match, then the tasks are sorted and grouped
    private void refresh() {
        Project project = getProject();
        if (project == null) return;

        String q = query.trim().toLowerCase();
        LocalDate today = LocalDate.now();

        ArrayList<Task> shownTasks = new ArrayList<>();
        for (Task task : project.getTasks()) {
            if (!q.isEmpty() && !task.getDescription().toLowerCase().contains(q)) continue;

            if (statusPos != 0 && task.getStatus().ordinal() != statusPos - 1) continue;

            if (priorityPos != 0 && task.getPriority().ordinal() != priorityPos - 1) continue;

            if (datePos == 1) {
                if (task.getDueDate() == null || !task.getDueDate().equals(today)) continue;
            } else if (datePos == 2) {
                if (!task.isOverdue()) continue;
            } else if (datePos == 3) {
                if (task.getDueDate() != null) continue;
            }

            shownTasks.add(task);
        }

        sortTasks(shownTasks);

        // group in the fixed order Overdue, In progress, To do, Done. empty groups get no header
        ArrayList<TaskRow> rows = new ArrayList<>();
        int[] order = {TaskRow.GROUP_OVERDUE, TaskRow.GROUP_IN_PROGRESS, TaskRow.GROUP_TODO, TaskRow.GROUP_DONE};
        for (int group : order) {
            ArrayList<Task> inGroup = new ArrayList<>();
            for (Task task : shownTasks) {
                if (groupOf(task) == group) inGroup.add(task);
            }
            if (inGroup.size() == 0) continue;

            rows.add(TaskRow.makeHeader(group, GROUP_NAMES[group], inGroup.size()));
            for (Task task : inGroup) {
                rows.add(TaskRow.makeTask(group, task));
            }
        }
        adapter.setRows(rows);

        int total = project.getTotalTasks();
        txtShowing.setText("Showing " + shownTasks.size() + " of " + total + " tasks");

        // two different empty states
        if (total == 0) {
            layoutNoTasks.setVisibility(View.VISIBLE);
            layoutNoMatch.setVisibility(View.GONE);
            recyclerTasks.setVisibility(View.GONE);
        } else if (shownTasks.size() == 0) {
            layoutNoTasks.setVisibility(View.GONE);
            layoutNoMatch.setVisibility(View.VISIBLE);
            recyclerTasks.setVisibility(View.GONE);
        } else {
            layoutNoTasks.setVisibility(View.GONE);
            layoutNoMatch.setVisibility(View.GONE);
            recyclerTasks.setVisibility(View.VISIBLE);
        }
    }

    private void sortTasks(ArrayList<Task> list) {
        // manual order keeps the order of the project's own task list (what the user dragged)
        if (sortPos == SORT_MANUAL) return;

        Collections.sort(list, (a, b) -> {
            int result;
            if (sortPos == 0) {
                result = compareDue(a, b, false);
            } else if (sortPos == 1) {
                result = compareDue(a, b, true);
            } else if (sortPos == 2) {
                result = a.getPriority().ordinal() - b.getPriority().ordinal();
                if (result == 0) result = compareDue(a, b, false);
            } else {
                result = b.getId() - a.getId();
            }
            if (result == 0) result = a.getId() - b.getId();
            return result;
        });
    }

    // tasks without a due date always go to the end
    private int compareDue(Task a, Task b, boolean descending) {
        if (a.getDueDate() == null && b.getDueDate() == null) return 0;
        if (a.getDueDate() == null) return 1;
        if (b.getDueDate() == null) return -1;
        int result = a.getDueDate().compareTo(b.getDueDate());
        if (descending) result = -result;
        return result;
    }

    // existing == null means add, otherwise edit
    private void showTaskDialog(Task existing) {
        Project project = getProject();
        DialogHelper.showTaskDialog(requireContext(), existing, new DialogHelper.TaskListener() {
            @Override
            public void onSave(String description, Task.Priority priority, Task.Status status, LocalDate dueDate) {
                if (existing == null) {
                    project.addTask(new Task(description, priority, status, dueDate));
                } else {
                    existing.setDescription(description);
                    existing.setPriority(priority);
                    existing.setStatus(status);
                    existing.setDueDate(dueDate);
                }
                host.onProjectChanged();
                refresh();
            }

            @Override
            public void onDelete() {
                project.removeTask(existing);
                host.onProjectChanged();
                refresh();
            }
        });
    }

    // left = mark Done, right = delete (after asking), long press + move = reorder inside the same group
    private class TaskTouchCallback extends ItemTouchHelper.Callback {

        private boolean dragged = false;
        private Paint paint = new Paint();

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return 0;

            // header rows can not be dragged or swiped
            if (adapter.getRow(position).isHeader()) {
                return makeMovementFlags(0, 0);
            }
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        // a row may only be dropped over a task row of its own group
        @Override
        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
            int from = current.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;

            TaskRow fromRow = adapter.getRow(from);
            TaskRow toRow = adapter.getRow(to);
            return !toRow.isHeader() && fromRow.getGroup() == toRow.getGroup();
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;

            TaskRow fromRow = adapter.getRow(from);
            TaskRow toRow = adapter.getRow(to);
            if (toRow.isHeader() || fromRow.getGroup() != toRow.getGroup()) return false;

            adapter.moveRow(from, to);
            dragged = true;
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            TaskRow row = adapter.getRow(position);
            Project project = getProject();
            Task task = project.findTaskById(row.getTaskId());
            if (task == null) {
                adapter.notifyItemChanged(position);
                return;
            }

            if (direction == ItemTouchHelper.LEFT) {
                if (task.getStatus() == Task.Status.DONE) {
                    Toast.makeText(requireContext(), "Task already completed", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);   // puts the row back
                    return;
                }
                task.setStatus(Task.Status.DONE);
                host.onProjectChanged();
                refresh();
            } else {
                // right: ask first. cancel puts the row back
                DialogHelper.showDeleteTaskDialog(requireContext(), task.getDescription(), () -> {
                    project.removeTask(task);
                    host.onProjectChanged();
                    refresh();
                }, () -> adapter.notifyItemChanged(position));
            }
        }

        // after a drop, save the order the user made into the project's task list
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (dragged) {
                dragged = false;
                Project project = getProject();

                ArrayList<Task> ordered = new ArrayList<>();
                for (TaskRow row : adapter.getRows()) {
                    if (row.isHeader()) continue;
                    Task task = project.findTaskById(row.getTaskId());
                    if (task != null) ordered.add(task);
                }
                project.reorderTasks(ordered);

                // from now on the list follows the manual order
                sortPos = SORT_MANUAL;
                syncing = true;
                spnSort.setSelection(SORT_MANUAL);
                syncing = false;
            }
        }

        // colored background behind a swiped row: green Done on the left swipe, red Delete on the right swipe
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View item = viewHolder.itemView;
                paint.setTextSize(36f);
                paint.setFakeBoldText(true);
                paint.setColor(Color.WHITE);

                if (dX < 0) {
                    Paint bg = new Paint();
                    bg.setColor(Color.parseColor("#22A06B"));
                    c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), bg);
                    c.drawText("Done", item.getRight() - 140f, item.getTop() + item.getHeight() / 2f + 12f, paint);
                } else if (dX > 0) {
                    Paint bg = new Paint();
                    bg.setColor(Color.parseColor("#E5484D"));
                    c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), bg);
                    c.drawText("Delete", item.getLeft() + 40f, item.getTop() + item.getHeight() / 2f + 12f, paint);
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
