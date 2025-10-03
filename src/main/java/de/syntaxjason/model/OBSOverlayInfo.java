package de.syntaxjason.model;

public class OBSOverlayInfo {
    private String sessionId;
    private String overlayUrl;
    private int width;
    private int height;

    public OBSOverlayInfo() {
        this.width = 1920;
        this.height = 200;
    }

    public OBSOverlayInfo(String sessionId, String overlayUrl) {
        this();
        this.sessionId = sessionId;
        this.overlayUrl = overlayUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getOverlayUrl() {
        return overlayUrl;
    }

    public void setOverlayUrl(String overlayUrl) {
        this.overlayUrl = overlayUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFullUrl() {
        return overlayUrl + "?session=" + sessionId;
    }
}
