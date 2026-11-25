package com.example.taskflow;

import java.util.ArrayList;

// project model. every total is worked out from the lists, nothing is stored twice
public class Project {

    private static int nextId = 1;

    private int id;
    private String tag;
    private String name;
    private String description;
    private int commentCount;
    private ArrayList<Task> tasks;
    private ArrayList<AttachmentLink> attachments;

    public Project(String tag, String name, String description, int commentCount) {
        this.id = nextId++;
        this.tag = tag;
        this.name = name;
        this.description = description;
        this.commentCount = commentCount;
        this.tasks = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    public int getId() { return id; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCommentCount() { return commentCount; }

    public ArrayList<Task> getTasks() { return tasks; }
    public ArrayList<AttachmentLink> getAttachments() { return attachments; }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public Task findTaskById(int taskId) {
        for (Task t : tasks) {
            if (t.getId() == taskId) return t;
        }
        return null;
    }

    public AttachmentLink findAttachmentById(int linkId) {
        for (AttachmentLink link : attachments) {
            if (link.getId() == linkId) return link;
        }
        return null;
    }

    // puts the given tasks into the slots they already use in the task list, in the new order.
    // tasks that are not in "newOrder" (hidden by a filter) keep their own slots
    public void reorderTasks(ArrayList<Task> newOrder) {
        ArrayList<Integer> slots = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (newOrder.contains(tasks.get(i))) {
                slots.add(i);
            }
        }
        for (int i = 0; i < slots.size() && i < newOrder.size(); i++) {
            tasks.set(slots.get(i), newOrder.get(i));
        }
    }

    public int getTotalTasks() {
        return tasks.size();
    }

    private int countStatus(Task.Status status) {
        int count = 0;
        for (Task t : tasks) {
            if (t.getStatus() == status) count++;
        }
        return count;
    }

    public int getTodoCount() { return countStatus(Task.Status.TODO); }
    public int getInProgressCount() { return countStatus(Task.Status.IN_PROGRESS); }
    public int getDoneCount() { return countStatus(Task.Status.DONE); }

    // overdue is only a subset of the unfinished tasks, it is not added to the other three
    public int getOverdueCount() {
        int count = 0;
        for (Task t : tasks) {
            if (t.isOverdue()) count++;
        }
        return count;
    }

    // done / total * 100, fractional part removed (4/12 = 33)
    public int getPercent() {
        if (tasks.size() == 0) return 0;
        return getDoneCount() * 100 / tasks.size();
    }

    public void linkCatalogItem(CatalogItem item) {
        attachments.add(new AttachmentLink(item.getId(), item.getResourceRef()));
    }

    public boolean hasCatalogItem(int catalogId) {
        for (AttachmentLink link : attachments) {
            if (link.getCatalogId() == catalogId) return true;
        }
        return false;
    }

    public void removeAttachment(AttachmentLink link) {
        attachments.remove(link);
    }

    public int getImageCount() {
        int count = 0;
        for (AttachmentLink link : attachments) {
            CatalogItem item = AppData.findCatalogItem(link.getCatalogId());
            if (item != null && item.getType() == CatalogItem.Type.IMAGE) count++;
        }
        return count;
    }

    public int getTextCount() {
        int count = 0;
        for (AttachmentLink link : attachments) {
            CatalogItem item = AppData.findCatalogItem(link.getCatalogId());
            if (item != null && item.getType() == CatalogItem.Type.TEXT) count++;
        }
        return count;
    }
}
