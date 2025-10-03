package de.syntaxjason.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class ActiveMultiplier {
    private final double multiplier;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String activatedBy;

    public ActiveMultiplier(double multiplier, int durationMinutes, String activatedBy) {
        this.multiplier = multiplier;
        this.startTime = LocalDateTime.now();
        this.endTime = startTime.plusMinutes(durationMinutes);
        this.activatedBy = activatedBy;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getActivatedBy() {
        return activatedBy;
    }

    public boolean isActive() {
        return LocalDateTime.now().isBefore(endTime);
    }

    public long getRemainingMinutes() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endTime)) {
            return 0;
        }

        return Duration.between(now, endTime).toMinutes();
    }
}
