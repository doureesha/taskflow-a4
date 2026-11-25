package com.example.taskflow;

// a frozen copy of one attachment link for the Files list (DiffUtil compares these copies)
public class AttachmentRow {

    private int linkId;
    private int catalogId;
    private String name;
    private String meta;
    private boolean image;

    public AttachmentRow(AttachmentLink link, CatalogItem item) {
        this.linkId = link.getId();
        this.catalogId = item.getId();
        this.name = item.getDisplayName();
        this.meta = item.typeLabel() + " • " + item.getSizeLabel();
        this.image = item.getType() == CatalogItem.Type.IMAGE;
    }

    public int getLinkId() { return linkId; }
    public int getCatalogId() { return catalogId; }
    public String getName() { return name; }
    public String getMeta() { return meta; }
    public boolean isImage() { return image; }

    public boolean sameContent(AttachmentRow other) {
        return name.equals(other.name) && meta.equals(other.meta) && image == other.image;
    }
}
