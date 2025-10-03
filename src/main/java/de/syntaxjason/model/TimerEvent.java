package de.syntaxjason.model;

import java.time.LocalDateTime;

public class TimerEvent {
    private EventType eventType;
    private String username;
    private String channelName;
    private int minutesAdded;
    private LocalDateTime timestamp;
    private String details;

    private TimerEvent(Builder builder) {
        this.eventType = builder.eventType;
        this.username = builder.username;
        this.channelName = builder.channelName;
        this.minutesAdded = builder.minutesAdded;
        this.timestamp = builder.timestamp;
        this.details = builder.details;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getUsername() {
        return username;
    }

    public String getChannelName() {
        return channelName;
    }

    public int getMinutesAdded() {
        return minutesAdded;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public static class Builder {
        private EventType eventType;
        private String username;
        private String channelName;
        private int minutesAdded;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String details = "";

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder channelName(String channelName) {
            this.channelName = channelName;
            return this;
        }

        public Builder minutesAdded(int minutesAdded) {
            this.minutesAdded = minutesAdded;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public TimerEvent build() {
            return new TimerEvent(this);
        }
    }
}
