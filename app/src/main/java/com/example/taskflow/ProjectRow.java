package com.example.taskflow;

// a frozen copy of one project for the Home list. DiffUtil compares these copies,
// because the real Project object is the same object before and after a change
public class ProjectRow {

    private int id;
    private String tag;
    private String name;
    private String description;
    private int done;
    private int total;
    private int percent;
    private int attachmentCount;
    private int commentCount;
    private boolean showInvite;

    public ProjectRow(Project project, boolean showInvite) {
        this.id = project.getId();
        this.tag = project.getTag();
        this.name = project.getName();
        this.description = project.getDescription();
        this.done = project.getDoneCount();
        this.total = project.getTotalTasks();
        this.percent = project.getPercent();
        this.attachmentCount = project.getAttachments().size();
        this.commentCount = project.getCommentCount();
        this.showInvite = showInvite;
    }

    public int getId() { return id; }
    public String getTag() { return tag; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDone() { return done; }
    public int getTotal() { return total; }
    public int getPercent() { return percent; }
    public int getAttachmentCount() { return attachmentCount; }
    public int getCommentCount() { return commentCount; }
    public boolean isShowInvite() { return showInvite; }

    public boolean sameContent(ProjectRow other) {
        return tag.equals(other.tag)
                && name.equals(other.name)
                && description.equals(other.description)
                && done == other.done
                && total == other.total
                && percent == other.percent
                && attachmentCount == other.attachmentCount
                && commentCount == other.commentCount
                && showInvite == other.showInvite;
    }
}
