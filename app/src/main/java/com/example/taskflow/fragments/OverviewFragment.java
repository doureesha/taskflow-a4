package com.example.taskflow.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.taskflow.AppData;
import com.example.taskflow.DialogHelper;
import com.example.taskflow.Project;
import com.example.taskflow.ProjectHost;
import com.example.taskflow.R;
import com.example.taskflow.Task;

public class OverviewFragment extends Fragment {

    private int projectId;
    private ProjectHost host;

    private TextView txtDescription, txtEditProject;
    private TextView txtCompleted, txtPercent;
    private ProgressBar progressOverview;
    private TextView txtTodo, txtInProgress, txtDone, txtOverdue;
    private View cardTodo, cardInProgress, cardDone, cardOverdue;
    private View linkAttachments, linkDiscussion;
    private TextView txtAttachmentsInfo, txtDiscussionInfo;
    private Button btnAddTask;

    public static OverviewFragment newInstance(int projectId) {
        OverviewFragment fragment = new OverviewFragment();
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
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        txtDescription = view.findViewById(R.id.txtOverviewDescription);
        txtEditProject = view.findViewById(R.id.txtEditProject);
        txtCompleted = view.findViewById(R.id.txtOverviewCompleted);
        txtPercent = view.findViewById(R.id.txtOverviewPercent);
        progressOverview = view.findViewById(R.id.progressOverview);
        txtTodo = view.findViewById(R.id.txtCountTodo);
        txtInProgress = view.findViewById(R.id.txtCountInProgress);
        txtDone = view.findViewById(R.id.txtCountDone);
        txtOverdue = view.findViewById(R.id.txtCountOverdue);
        cardTodo = view.findViewById(R.id.cardTodo);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardDone = view.findViewById(R.id.cardDone);
        cardOverdue = view.findViewById(R.id.cardOverdue);
        linkAttachments = view.findViewById(R.id.linkAttachments);
        linkDiscussion = view.findViewById(R.id.linkDiscussion);
        txtAttachmentsInfo = view.findViewById(R.id.txtAttachmentsInfo);
        txtDiscussionInfo = view.findViewById(R.id.txtDiscussionInfo);
        btnAddTask = view.findViewById(R.id.btnOverviewAddTask);

        txtEditProject.setOnClickListener(v -> showEditDialog());

        // status cards jump to the Tasks tab with the matching filter
        cardTodo.setOnClickListener(v -> host.openTasksWithFilter(Task.Status.TODO, false));
        cardInProgress.setOnClickListener(v -> host.openTasksWithFilter(Task.Status.IN_PROGRESS, false));
        cardDone.setOnClickListener(v -> host.openTasksWithFilter(Task.Status.DONE, false));
        cardOverdue.setOnClickListener(v -> host.openTasksWithFilter(null, true));

        linkAttachments.setOnClickListener(v -> host.openTab(ProjectHost.TAB_FILES));
        linkDiscussion.setOnClickListener(v -> host.openTab(ProjectHost.TAB_DISCUSSION));

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    // read the project again every time this tab becomes visible
    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private Project getProject() {
        return AppData.findProjectById(projectId);
    }

    private void refresh() {
        Project project = getProject();
        if (project == null) return;

        txtDescription.setText(project.getDescription());

        txtCompleted.setText(project.getDoneCount() + " of " + project.getTotalTasks() + " tasks completed");
        txtPercent.setText(project.getPercent() + "%");
        progressOverview.setProgress(project.getPercent());

        txtTodo.setText(String.valueOf(project.getTodoCount()));
        txtInProgress.setText(String.valueOf(project.getInProgressCount()));
        txtDone.setText(String.valueOf(project.getDoneCount()));
        txtOverdue.setText(String.valueOf(project.getOverdueCount()));

        txtAttachmentsInfo.setText(project.getAttachments().size() + " files");
        txtDiscussionInfo.setText(project.getCommentCount() + " comments");
    }

    private void showEditDialog() {
        Project project = getProject();
        DialogHelper.showProjectDialog(requireContext(), project, (name, tag, description, attachment) -> {
            project.setName(name);
            project.setTag(tag);
            project.setDescription(description);
            host.onProjectChanged();
            refresh();
        });
    }

    // same dialog the Tasks tab uses. after saving we stay on Overview
    private void showAddTaskDialog() {
        Project project = getProject();
        DialogHelper.showTaskDialog(requireContext(), null, new DialogHelper.TaskListener() {
            @Override
            public void onSave(String description, Task.Priority priority, Task.Status status, java.time.LocalDate dueDate) {
                project.addTask(new Task(description, priority, status, dueDate));
                host.onProjectChanged();
                refresh();
            }

            @Override
            public void onDelete() { }
        });
    }
}
