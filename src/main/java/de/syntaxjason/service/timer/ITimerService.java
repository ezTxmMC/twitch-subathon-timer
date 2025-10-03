package de.syntaxjason.service.timer;

import java.time.Duration;
import java.util.function.Consumer;

public interface ITimerService {
    Duration getRemainingTime();
    void setRemainingTime(Duration duration);
    void addMinutes(int minutes);
    void subtractSecond();
    void reset();
    boolean isFinished();
    void registerTimeChangeListener(Consumer<Duration> listener);
}
