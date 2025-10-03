package de.syntaxjason.model;

public enum SessionStatus {
    ACTIVE("Aktiv"),
    COMPLETED("Abgeschlossen"),
    ARCHIVED("Archiviert");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
