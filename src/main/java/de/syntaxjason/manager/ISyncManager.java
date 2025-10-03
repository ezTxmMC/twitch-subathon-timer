package de.syntaxjason.manager;

public interface ISyncManager {
    void initialize();
    void startSync();
    void stopSync();
    boolean isSyncing();
}
