package com.example.taskflow;

// the fragments talk to ProjectActivity only through this interface (host callback)
public interface ProjectHost {

    int TAB_OVERVIEW = 0;
    int TAB_TASKS = 1;
    int TAB_FILES = 2;
    int TAB_DISCUSSION = 3;

    // a request from Overview to open Tasks with a filter already applied
    class FilterRequest {
        public Task.Status status;   // null means all statuses
        public boolean overdue;
    }

    // call after anything changed so header, tab counts and the result for Home are updated
    void onProjectChanged();

    void openTab(int index);

    void openTasksWithFilter(Task.Status status, boolean overdue);

    // Tasks fragment calls this when it becomes visible. returns null when nothing is waiting
    FilterRequest takePendingFilter();
}
