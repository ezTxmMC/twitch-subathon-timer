package de.syntaxjason.model;

public class TimerStateUpdate {
    private long remainingSeconds;
    private boolean isPaused;

    public TimerStateUpdate() {
    }

    public TimerStateUpdate(long remainingSeconds, boolean isPaused) {
        this.remainingSeconds = remainingSeconds;
        this.isPaused = isPaused;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
