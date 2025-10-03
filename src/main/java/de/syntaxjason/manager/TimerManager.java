package de.syntaxjason.manager;

import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.multiplier.IMultiplierService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.session.ISessionService;
import de.syntaxjason.service.timer.ITimerService;

import javax.swing.SwingUtilities;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TimerManager implements ITimerManager {
    private final ITimerService timerService;
    private final IConfigService configService;
    private final IMultiplierService multiplierService;
    private final ISessionService sessionService;
    private final IServerService serverService;
    private final List<Consumer<TimerEvent>> eventListeners;
    private final List<Consumer<Boolean>> pauseListeners;
    private Thread timerThread;
    private volatile boolean running;
    private volatile boolean paused;
    private int secondsCounter = 0;
    private int serverSyncCounter = 0;
    private boolean ignoreNextPauseSync = false;
    private boolean ignoreNextResumeSync = false;
    private boolean isHost = false;

    public TimerManager(ITimerService timerService, IConfigService configService, IMultiplierService multiplierService, ISessionService sessionService, IServerService serverService) {
        this.timerService = timerService;
        this.configService = configService;
        this.multiplierService = multiplierService;
        this.sessionService = sessionService;
        this.serverService = serverService;
        this.eventListeners = new ArrayList<>();
        this.pauseListeners = new ArrayList<>();
        this.running = false;
        this.paused = false;
    }

    @Override
    public void setHost(boolean isHost) {
        this.isHost = isHost;
        System.out.println("Timer Host-Modus: " + (isHost ? "Aktiv" : "Inaktiv"));
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        paused = false;

        if (isHost) {
            timerThread = new Thread(this::runTimer);
            timerThread.setDaemon(true);
            timerThread.start();
            System.out.println("Timer gestartet (Host-Modus)");
        } else {
            System.out.println("Timer gestartet (Client-Modus - nur Sync)");
        }
    }

    @Override
    public void pause() {
        pause(true);
    }

    public void pause(boolean sendToServer) {
        if (!running) {
            return;
        }

        paused = true;
        notifyPauseListeners(true);
        updateSessionRemainingTime();

        if (isHost) {
            syncToServer();
        }

        if (sendToServer && serverService.isConnected()) {
            ignoreNextPauseSync = true;
            serverService.sendPauseRequest();
        }

        System.out.println("Timer pausiert" + (sendToServer ? " (an Server gesendet)" : " (von Server empfangen)"));
    }

    @Override
    public void resume() {
        resume(true);
    }

    public void resume(boolean sendToServer) {
        if (!running) {
            return;
        }

        paused = false;
        notifyPauseListeners(false);

        if (isHost) {
            syncToServer();
        }

        if (sendToServer && serverService.isConnected()) {
            ignoreNextResumeSync = true;
            serverService.sendResumeRequest();
        }

        System.out.println("Timer fortgesetzt" + (sendToServer ? " (an Server gesendet)" : " (von Server empfangen)"));
    }

    public boolean shouldIgnoreNextPauseSync() {
        if (ignoreNextPauseSync) {
            ignoreNextPauseSync = false;
            return true;
        }
        return false;
    }

    public boolean shouldIgnoreNextResumeSync() {
        if (ignoreNextResumeSync) {
            ignoreNextResumeSync = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    private void runTimer() {
        while (running) {
            try {
                Thread.sleep(1000);

                if (paused) {
                    continue;
                }

                if (timerService.isFinished()) {
                    handleTimerFinished();
                    continue;
                }

                timerService.subtractSecond();
                multiplierService.clearExpiredMultipliers();

                secondsCounter++;
                serverSyncCounter++;

                if (secondsCounter >= 10) {
                    updateSessionRemainingTime();
                    secondsCounter = 0;
                }

                if (serverSyncCounter >= 1) {
                    syncToServer();
                    serverSyncCounter = 0;
                }

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateSessionRemainingTime() {
        Duration remaining = timerService.getRemainingTime();
        sessionService.updateRemainingTime(remaining);
    }

    private void syncToServer() {
        if (!serverService.isConnected() || !isHost) {
            return;
        }

        Duration remaining = timerService.getRemainingTime();
        serverService.sendTimerUpdate(remaining, paused);
    }

    private void handleTimerFinished() {
        if (sessionService.getCurrentSession() != null) {
            sessionService.endCurrentSession();
            System.out.println("Session automatisch beendet - Timer abgelaufen");
        }
    }

    @Override
    public void stop() {
        running = false;
        paused = false;

        updateSessionRemainingTime();

        if (timerThread != null) {
            timerThread.interrupt();
        }

        System.out.println("Timer gestoppt");
    }

    @Override
    public void processEvent(TimerEvent event) {
        if (event == null) {
            return;
        }

        double multiplier = multiplierService.getCurrentMultiplier();
        int baseMinutes = event.getMinutesAdded();
        int finalMinutes = (int) Math.round(baseMinutes * multiplier);

        timerService.addMinutes(finalMinutes);
        sessionService.addEventToCurrentSession(event);
        notifyEventListeners(event);

        if (serverService.isConnected()) {
            serverService.sendEvent(event);
        }

        if (multiplier > 1.0) {
            System.out.println(String.format(
                    "Event mit Multiplier: %d Min x %.1fx = %d Min",
                    baseMinutes,
                    multiplier,
                    finalMinutes
            ));
        }
    }

    @Override
    public void registerEventListener(Consumer<TimerEvent> listener) {
        if (listener == null) {
            return;
        }

        eventListeners.add(listener);
    }

    @Override
    public void registerPauseListener(Consumer<Boolean> listener) {
        if (listener == null) {
            return;
        }

        pauseListeners.add(listener);
    }

    private void notifyEventListeners(TimerEvent event) {
        SwingUtilities.invokeLater(() -> {
            for (Consumer<TimerEvent> listener : eventListeners) {
                listener.accept(event);
            }
        });
    }

    private void notifyPauseListeners(boolean isPaused) {
        SwingUtilities.invokeLater(() -> {
            for (Consumer<Boolean> listener : pauseListeners) {
                listener.accept(isPaused);
            }
        });
    }
}
