package de.syntaxjason.model;

public enum ServerConnectionStatus {
    DISCONNECTED("Getrennt"),
    CONNECTING("Verbinde..."),
    CONNECTED("Verbunden"),
    ERROR("Fehler");

    private final String displayName;

    ServerConnectionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

