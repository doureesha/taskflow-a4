package com.example.taskflow;

// one file in the local catalog. image items hold a drawable id, text items hold the actual text
public class CatalogItem {

    public enum Type { IMAGE, TEXT }

    private int id;
    private String displayName;
    private Type type;
    private int imageRes;
    private String textContent;
    private String sizeLabel;
    private String resourceRef;

    public CatalogItem(int id, String displayName, Type type, int imageRes, String textContent, String sizeLabel, String resourceRef) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.imageRes = imageRes;
        this.textContent = textContent;
        this.sizeLabel = sizeLabel;
        this.resourceRef = resourceRef;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Type getType() { return type; }
    public int getImageRes() { return imageRes; }
    public String getTextContent() { return textContent; }
    public String getSizeLabel() { return sizeLabel; }
    public String getResourceRef() { return resourceRef; }

    public String typeLabel() {
        if (type == Type.IMAGE) return "Image";
        return "Text";
    }
}
