package de.syntaxjason.model;

public enum EventType {
    FOLLOWER("Follower"),
    RAID("Raid"),
    SUB("Subscription"),
    BITS("Bits"),
    SUBGIFT("Subgift");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
