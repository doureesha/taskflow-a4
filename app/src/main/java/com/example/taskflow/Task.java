package com.example.taskflow;

import java.time.LocalDate;

// a single task inside a project. status/priority are plain enums - basic oop, no libraries
public class Task {

    public enum Status { TODO, IN_PROGRESS, DONE }
    public enum Priority { HIGH, NORMAL, LOW }

    private static int nextId = 1;

    private int id;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;   // nullable - no due date set
    private long createdAt;

    public Task(String description, Priority priority, Status status, LocalDate dueDate) {
        this.id = nextId++;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public long getCreatedAt() { return createdAt; }

    // overdue = unfinished AND due date is strictly before today. today/no-due-date are never overdue
    public boolean isOverdue() {
        if (status == Status.DONE) return false;
        if (dueDate == null) return false;
        return dueDate.isBefore(LocalDate.now());
    }

    public static String formatDate(LocalDate date) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return date.getDayOfMonth() + " " + months[date.getMonthValue() - 1] + " " + date.getYear();
    }

    public String statusLabel() {
        switch (status) {
            case TODO: return "To do";
            case IN_PROGRESS: return "In progress";
            case DONE: return "Done";
        }
        return "";
    }

    public String priorityLabel() {
        switch (priority) {
            case HIGH: return "High";
            case NORMAL: return "Normal";
            case LOW: return "Low";
        }
        return "";
    }
}
