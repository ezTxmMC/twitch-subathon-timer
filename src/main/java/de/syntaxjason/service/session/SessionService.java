package de.syntaxjason.service.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.syntaxjason.model.Session;
import de.syntaxjason.model.SessionStatus;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.service.timer.ITimerService;
import de.syntaxjason.util.LocalDateTimeAdapter;
import de.syntaxjason.util.PathManager;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionService implements ISessionService {
    private Session currentSession;
    private final List<Session> sessions;
    private final Gson gson;
    private final Path sessionsFile;
    private ITimerService timerService;
    private final String myChannelName;

    public SessionService(String myChannelName) {
        this.myChannelName = myChannelName;
        this.sessions = new ArrayList<>();
        this.sessionsFile = PathManager.getAppDataPath().resolve("sessions.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void setTimerService(ITimerService timerService) {
        this.timerService = timerService;
    }

    @Override
    public void initialize() {
        loadSessions();

        Session active = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (active != null) {
            this.currentSession = active;
            restoreTimerFromSession(active);
            System.out.println("Session wiederhergestellt: " + active.getName() +
                    " (Verbleibend: " + active.getRemainingMinutes() + " Min)");
            return;
        }

        startNewSession("Standard Session", 240);
    }

    private void restoreTimerFromSession(Session session) {
        if (timerService == null) {
            return;
        }

        Duration remainingTime = Duration.ofMinutes(session.getRemainingMinutes());
        timerService.setRemainingTime(remainingTime);
    }

    @Override
    public Session getCurrentSession() {
        return currentSession;
    }

    @Override
    public void startNewSession(String name, long initialMinutes) {
        if (currentSession != null && currentSession.getStatus() == SessionStatus.ACTIVE) {
            endCurrentSession();
        }

        currentSession = new Session(name, initialMinutes);
        sessions.add(currentSession);
        saveCurrentSession();

        if (timerService != null) {
            timerService.setRemainingTime(Duration.ofMinutes(initialMinutes));
        }
    }

    @Override
    public void endCurrentSession() {
        if (currentSession == null) {
            return;
        }

        currentSession.end();
        saveCurrentSession();
    }

    @Override
    public void addEventToCurrentSession(TimerEvent event) {
        if (currentSession == null) {
            return;
        }

        currentSession.addEvent(event);
        updateCurrentSessionRemainingTime();
        saveCurrentSession();
    }

    @Override
    public void updateRemainingTime(Duration remainingTime) {
        if (currentSession == null) {
            return;
        }

        currentSession.setRemainingMinutes(remainingTime.toMinutes());
        saveCurrentSession();
    }

    private void updateCurrentSessionRemainingTime() {
        if (timerService == null || currentSession == null) {
            return;
        }

        Duration remaining = timerService.getRemainingTime();
        currentSession.setRemainingMinutes(remaining.toMinutes());
    }

    @Override
    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    @Override
    public List<Session> getActiveSessions() {
        return sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> getArchivedSessions() {
        return sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.ARCHIVED || s.getStatus() == SessionStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    @Override
    public void archiveSession(String sessionId) {
        Session session = findSessionById(sessionId);

        if (session == null) {
            return;
        }

        session.setStatus(SessionStatus.ARCHIVED);
        saveCurrentSession();
    }

    @Override
    public void deleteSession(String sessionId) {
        sessions.removeIf(s -> s.getId().equals(sessionId));
        saveCurrentSession();
    }

    @Override
    public void loadSession(String sessionId) {
        Session session = findSessionById(sessionId);

        if (session == null) {
            return;
        }

        if (currentSession != null && currentSession.getStatus() == SessionStatus.ACTIVE) {
            endCurrentSession();
        }

        this.currentSession = session;
        restoreTimerFromSession(session);
    }

    @Override
    public void saveCurrentSession() {
        saveSessions();
    }

    private Session findSessionById(String sessionId) {
        return sessions.stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElse(null);
    }

    private void loadSessions() {
        if (!Files.exists(sessionsFile)) {
            return;
        }

        try {
            String json = Files.readString(sessionsFile);
            TypeToken<List<Session>> typeToken = new TypeToken<>() {};
            List<Session> loadedSessions = gson.fromJson(json, typeToken.getType());

            if (loadedSessions != null) {
                sessions.addAll(loadedSessions);
            }

            System.out.println("Sessions geladen: " + sessions.size());
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Sessions");
            e.printStackTrace();
        }
    }

    private void saveSessions() {
        try (FileWriter writer = new FileWriter(sessionsFile.toFile())) {
            gson.toJson(sessions, writer);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Sessions");
            e.printStackTrace();
        }
    }
}
