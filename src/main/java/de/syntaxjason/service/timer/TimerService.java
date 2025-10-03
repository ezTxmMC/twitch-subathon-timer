package de.syntaxjason.service.timer;

import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.service.config.IConfigService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TimerService implements ITimerService {
    private final IConfigService configService;
    private Duration remainingTime;
    private final List<Consumer<Duration>> timeChangeListeners;

    public TimerService(IConfigService configService) {
        this.configService = configService;
        this.timeChangeListeners = new ArrayList<>();
        initializeRemainingTime();
    }

    private void initializeRemainingTime() {
        SabathonConfig config = configService.getConfig();

        if (config == null || config.getTimerSettings() == null) {
            this.remainingTime = Duration.ofMinutes(240);
            return;
        }

        this.remainingTime = Duration.ofMinutes(config.getTimerSettings().getTotalMinutes());
    }

    @Override
    public Duration getRemainingTime() {
        return remainingTime;
    }

    @Override
    public void setRemainingTime(Duration duration) {
        this.remainingTime = duration;
        notifyTimeChangeListeners();
    }

    @Override
    public void addMinutes(int minutes) {
        remainingTime = remainingTime.plusMinutes(minutes);
        notifyTimeChangeListeners();
    }

    @Override
    public void subtractSecond() {
        if (remainingTime.isZero() || remainingTime.isNegative()) {
            return;
        }

        remainingTime = remainingTime.minusSeconds(1);
        notifyTimeChangeListeners();
    }

    @Override
    public void reset() {
        SabathonConfig config = configService.getConfig();

        if (config == null || config.getTimerSettings() == null) {
            remainingTime = Duration.ofMinutes(240);
            notifyTimeChangeListeners();
            return;
        }

        remainingTime = Duration.ofMinutes(config.getTimerSettings().getTotalMinutes());
        notifyTimeChangeListeners();
    }

    @Override
    public boolean isFinished() {
        return remainingTime.isZero() || remainingTime.isNegative();
    }

    @Override
    public void registerTimeChangeListener(Consumer<Duration> listener) {
        timeChangeListeners.add(listener);
    }

    private void notifyTimeChangeListeners() {
        for (Consumer<Duration> listener : timeChangeListeners) {
            listener.accept(remainingTime);
        }
    }
}
