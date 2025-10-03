package de.syntaxjason.service.database;

import de.syntaxjason.model.BackupInfo;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.TimerEvent;
import java.nio.file.Path;
import java.util.List;

public interface IDatabaseService {
    void initialize();
    void saveEvent(TimerEvent event);
    List<TimerEvent> getAllEvents();
    List<TimerEvent> getEventsByUsername(String username);
    boolean hasRecentEvent(String username, EventType eventType, int hours);
    void clearAllEvents();
    void createBackup();
    List<BackupInfo> getAvailableBackups();
    boolean restoreBackup(Path backupPath);
}
