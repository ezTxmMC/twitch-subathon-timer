package de.syntaxjason.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private String id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long initialMinutes;
    private long remainingMinutes;
    private SessionStatus status;
    private List<TimerEvent> events;
    private Map<EventType, Integer> eventCounts;
    private Map<EventType, Integer> totalMinutesAdded;

    public Session() {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = "Session " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.ACTIVE;
        this.events = new ArrayList<>();
        this.eventCounts = new HashMap<>();
        this.totalMinutesAdded = new HashMap<>();
        initializeCounts();
    }

    public Session(String name, long initialMinutes) {
        this();
        this.name = name;
        this.initialMinutes = initialMinutes;
        this.remainingMinutes = initialMinutes;
    }

    private void initializeCounts() {
        for (EventType type : EventType.values()) {
            eventCounts.put(type, 0);
            totalMinutesAdded.put(type, 0);
        }
    }

    public void addEvent(TimerEvent event) {
        events.add(event);

        EventType type = event.getEventType();
        eventCounts.put(type, eventCounts.getOrDefault(type, 0) + 1);
        totalMinutesAdded.put(type, totalMinutesAdded.getOrDefault(type, 0) + event.getMinutesAdded());
    }

    public void end() {
        this.endTime = LocalDateTime.now();
        this.status = SessionStatus.COMPLETED;
    }

    public long getDurationMinutes() {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMinutes();
    }

    public int getTotalEvents() {
        return events.size();
    }

    public int getTotalMinutesAdded() {
        return totalMinutesAdded.values().stream().mapToInt(Integer::intValue).sum();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public List<TimerEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TimerEvent> events) {
        this.events = events;
    }

    public Map<EventType, Integer> getEventCounts() {
        return eventCounts;
    }

    public void setEventCounts(Map<EventType, Integer> eventCounts) {
        this.eventCounts = eventCounts;
    }

    public Map<EventType, Integer> getTotalMinutesAddedMap() {
        return totalMinutesAdded;
    }

    public void setTotalMinutesAdded(Map<EventType, Integer> totalMinutesAdded) {
        this.totalMinutesAdded = totalMinutesAdded;
    }
}
