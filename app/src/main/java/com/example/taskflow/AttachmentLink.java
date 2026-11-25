package com.example.taskflow;

// a link between a project and a catalog item. stores only the reference, never the image itself
public class AttachmentLink {

    private static int nextId = 1;

    private int id;
    private int catalogId;
    private String resourceRef;
    private long linkedAt;

    public AttachmentLink(int catalogId, String resourceRef) {
        this.id = nextId++;
        this.catalogId = catalogId;
        this.resourceRef = resourceRef;
        this.linkedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public int getCatalogId() { return catalogId; }
    public String getResourceRef() { return resourceRef; }
    public long getLinkedAt() { return linkedAt; }
}
