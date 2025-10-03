package de.syntaxjason.manager;

import de.syntaxjason.model.TimerEvent;
import java.util.function.Consumer;

public interface ITimerManager {
    void start();
    void pause();
    void resume();
    void stop();
    boolean isPaused();
    void processEvent(TimerEvent event);
    void registerEventListener(Consumer<TimerEvent> listener);
    void registerPauseListener(Consumer<Boolean> listener);
    void setHost(boolean isHost);
}
