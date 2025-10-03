package de.syntaxjason.service.session;

import de.syntaxjason.model.Session;
import de.syntaxjason.model.TimerEvent;
import java.time.Duration;
import java.util.List;

public interface ISessionService {
    void initialize();
    Session getCurrentSession();
    void startNewSession(String name, long initialMinutes);
    void endCurrentSession();
    void addEventToCurrentSession(TimerEvent event);
    void updateRemainingTime(Duration remainingTime);
    List<Session> getAllSessions();
    List<Session> getActiveSessions();
    List<Session> getArchivedSessions();
    void archiveSession(String sessionId);
    void deleteSession(String sessionId);
    void loadSession(String sessionId);
    void saveCurrentSession();
}
