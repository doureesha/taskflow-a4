package com.example.taskflow;

// one line of the grouped task list. it is either a header ("Overdue  2") or a task.
// it is a frozen copy so DiffUtil can compare the old list with the new list
public class TaskRow {

    public static final int GROUP_OVERDUE = 0;
    public static final int GROUP_IN_PROGRESS = 1;
    public static final int GROUP_TODO = 2;
    public static final int GROUP_DONE = 3;

    private boolean header;
    private int group;

    // header only
    private String headerTitle;
    private int headerCount;

    // task only
    private int taskId;
    private String description;
    private int statusIndex;
    private int priorityIndex;
    private String dueText;
    private boolean overdue;
    private boolean done;

    public static TaskRow makeHeader(int group, String title, int count) {
        TaskRow row = new TaskRow();
        row.header = true;
        row.group = group;
        row.headerTitle = title;
        row.headerCount = count;
        return row;
    }

    public static TaskRow makeTask(int group, Task task) {
        TaskRow row = new TaskRow();
        row.header = false;
        row.group = group;
        row.taskId = task.getId();
        row.description = task.getDescription();
        row.statusIndex = task.getStatus().ordinal();
        row.priorityIndex = task.getPriority().ordinal();
        if (task.getDueDate() == null) {
            row.dueText = "No due date";
        } else {
            row.dueText = Task.formatDate(task.getDueDate());
        }
        row.overdue = task.isOverdue();
        row.done = task.getStatus() == Task.Status.DONE;
        return row;
    }

    public boolean isHeader() { return header; }
    public int getGroup() { return group; }
    public String getHeaderTitle() { return headerTitle; }
    public int getHeaderCount() { return headerCount; }
    public int getTaskId() { return taskId; }
    public String getDescription() { return description; }
    public int getStatusIndex() { return statusIndex; }
    public int getPriorityIndex() { return priorityIndex; }
    public String getDueText() { return dueText; }
    public boolean isOverdue() { return overdue; }
    public boolean isDone() { return done; }

    // same line = same header group, or same task id
    public boolean sameItem(TaskRow other) {
        if (header != other.header) return false;
        if (header) return group == other.group;
        return taskId == other.taskId;
    }

    // same look on screen
    public boolean sameContent(TaskRow other) {
        if (header) {
            return headerTitle.equals(other.headerTitle) && headerCount == other.headerCount;
        }
        return description.equals(other.description)
                && statusIndex == other.statusIndex
                && priorityIndex == other.priorityIndex
                && dueText.equals(other.dueText)
                && overdue == other.overdue
                && done == other.done;
    }
}
