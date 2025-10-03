package de.syntaxjason.model;

public enum SessionRole {
    HOST("Host"),
    PARTICIPANT("Teilnehmer");

    private final String displayName;

    SessionRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
