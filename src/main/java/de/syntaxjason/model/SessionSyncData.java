package de.syntaxjason.model;

import java.util.List;

public class SessionSyncData {
    private String sessionId;
    private String sessionName;
    private long initialMinutes;
    private long remainingMinutes;
    private boolean isPaused;
    private SabathonConfig config;
    private List<TimerEvent> eventHistory;
    private ActiveMultiplier activeMultiplier;
    private Session currentSession;

    public SessionSyncData() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public long getInitialMinutes() {
        return initialMinutes;
    }

    public void setInitialMinutes(long initialMinutes) {
        this.initialMinutes = initialMinutes;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(long remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public SabathonConfig getConfig() {
        return config;
    }

    public void setConfig(SabathonConfig config) {
        this.config = config;
    }

    public List<TimerEvent> getEventHistory() {
        return eventHistory;
    }

    public void setEventHistory(List<TimerEvent> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public ActiveMultiplier getActiveMultiplier() {
        return activeMultiplier;
    }

    public void setActiveMultiplier(ActiveMultiplier activeMultiplier) {
        this.activeMultiplier = activeMultiplier;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }
}
