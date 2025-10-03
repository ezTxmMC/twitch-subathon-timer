package de.syntaxjason.manager;

import de.syntaxjason.model.*;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.multiplier.IMultiplierService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.session.ISessionService;
import de.syntaxjason.service.timer.ITimerService;

import javax.swing.SwingUtilities;
import java.time.Duration;
import java.util.List;

public class SyncManager implements ISyncManager {
    private final IServerService serverService;
    private final IConfigService configService;
    private final ITimerService timerService;
    private final IMultiplierService multiplierService;
    private final ISessionService sessionService;
    private final TimerManager timerManager;
    private boolean syncing = false;

    public SyncManager(IServerService serverService, IConfigService configService, ITimerService timerService, IMultiplierService multiplierService, ISessionService sessionService, ITimerManager timerManager) {
        this.serverService = serverService;
        this.configService = configService;
        this.timerService = timerService;
        this.multiplierService = multiplierService;
        this.sessionService = sessionService;
        this.timerManager = (TimerManager) timerManager;
    }

    @Override
    public void initialize() {
        setupListeners();
    }

    @Override
    public void startSync() {
        if (syncing) {
            return;
        }

        syncing = true;
        System.out.println("Synchronisation gestartet");
    }

    @Override
    public void stopSync() {
        syncing = false;
        System.out.println("Synchronisation gestoppt");
    }

    @Override
    public boolean isSyncing() {
        return syncing;
    }

    private void setupListeners() {
        serverService.registerFullSyncListener(this::handleFullSync);
        serverService.registerTimerUpdateListener(this::handleTimerUpdate);
        serverService.registerEventListener(this::handleEvent);
        serverService.registerConfigUpdateListener(this::handleConfigUpdate);
        serverService.registerChannelAddListener(this::handleChannelAdd);
        serverService.registerMultiplierListener(this::handleMultiplierUpdate);
        serverService.registerConnectionStatusListener(this::handleConnectionStatus);
        serverService.registerPauseListener(this::handlePause);
        serverService.registerResumeListener(this::handleResume);
    }

    private void handleFullSync(SessionSyncData syncData) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Full Sync empfangen - Synchronisiere alle Daten...");

            if (syncData.getConfig() != null) {
                syncConfig(syncData.getConfig());
            }

            if (syncData.getCurrentSession() != null) {
                syncSession(syncData.getCurrentSession());
            }

            Duration remaining = Duration.ofMinutes(syncData.getRemainingMinutes());
            timerService.setRemainingTime(remaining);

            if (syncData.isPaused()) {
                timerManager.pause(false);
            }

            if (syncData.getActiveMultiplier() != null) {
                syncMultiplier(syncData.getActiveMultiplier());
            }

            if (syncData.getEventHistory() != null) {
                syncEventHistory(syncData.getEventHistory());
            }

            startSync();
            System.out.println("Full Sync abgeschlossen");
        });
    }

    private void syncConfig(SabathonConfig remoteConfig) {
        SabathonConfig localConfig = configService.getConfig();

        System.out.println("=== CONFIG SYNC DEBUG ===");
        System.out.println("Remote Token: " + remoteConfig.getBotSettings().getBotAccessToken());
        System.out.println("Local Token (vorher): " + localConfig.getBotSettings().getBotAccessToken());

        String remoteToken = remoteConfig.getBotSettings().getBotAccessToken();
        if (remoteToken != null && !remoteToken.isEmpty()) {
            localConfig.getBotSettings().setBotAccessToken(remoteToken);
            System.out.println("OAuth Token synchronisiert");
        }

        System.out.println("Local Token (nachher): " + localConfig.getBotSettings().getBotAccessToken());
        System.out.println("========================");

        List<ChannelConfig> remoteChannels = remoteConfig.getChannels();
        for (ChannelConfig remoteChannel : remoteChannels) {
            boolean exists = localConfig.getChannels().stream()
                    .anyMatch(c -> c.getChannelName().equals(remoteChannel.getChannelName()));

            if (!exists) {
                localConfig.getChannels().add(remoteChannel);
                System.out.println("Channel hinzugefügt: " + remoteChannel.getChannelName());
            }
        }

        localConfig.setEventMinutes(EventType.FOLLOWER, remoteConfig.getEventMinutes(EventType.FOLLOWER));
        localConfig.setEventMinutes(EventType.RAID, remoteConfig.getEventMinutes(EventType.RAID));
        localConfig.setEventMinutes(EventType.SUB, remoteConfig.getEventMinutes(EventType.SUB));
        localConfig.setEventMinutes(EventType.BITS, remoteConfig.getEventMinutes(EventType.BITS));
        localConfig.setEventMinutes(EventType.SUBGIFT, remoteConfig.getEventMinutes(EventType.SUBGIFT));

        localConfig.setBitsSettings(remoteConfig.getBitsSettings());
        localConfig.setRaidSettings(remoteConfig.getRaidSettings());
        localConfig.setFollowerSettings(remoteConfig.getFollowerSettings());
        localConfig.setMultiplierRewards(remoteConfig.getMultiplierRewards());

        configService.saveConfig();
        System.out.println("Config vollständig synchronisiert");
    }

    private void syncSession(Session remoteSession) {
        sessionService.startNewSession(remoteSession.getName(), remoteSession.getInitialMinutes());
        System.out.println("Session synchronisiert: " + remoteSession.getName());
    }

    private void syncMultiplier(ActiveMultiplier remoteMultiplier) {
        System.out.println("Multiplier synchronisiert: " + remoteMultiplier.getMultiplier() + "x");
    }

    private void syncEventHistory(List<TimerEvent> events) {
        System.out.println("Event History synchronisiert: " + events.size() + " Events");
    }

    private void handleTimerUpdate(TimerStateUpdate update) {
        if (!syncing) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Duration newTime = Duration.ofSeconds(update.getRemainingSeconds());
            timerService.setRemainingTime(newTime);

            if (update.isPaused() && !timerManager.isPaused()) {
                timerManager.pause(false);
            }

            if (!update.isPaused() && timerManager.isPaused()) {
                timerManager.resume(false);
            }
        });
    }

    private void handleEvent(TimerEvent event) {
        if (!syncing) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            double multiplier = multiplierService.getCurrentMultiplier();
            int baseMinutes = event.getMinutesAdded();
            int finalMinutes = (int) Math.round(baseMinutes * multiplier);

            timerService.addMinutes(finalMinutes);
            sessionService.addEventToCurrentSession(event);

            System.out.println("Event empfangen: " + event.getEventType() +
                    " von " + event.getUsername() +
                    " @ " + event.getChannelName() +
                    " +" + finalMinutes + " Min");
        });
    }

    private void handleConfigUpdate(SabathonConfig config) {
        if (!syncing) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            syncConfig(config);
            System.out.println("Config Update empfangen und angewendet");
        });
    }

    private void handleChannelAdd(String channelName) {
        if (!syncing) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            SabathonConfig config = configService.getConfig();

            boolean exists = config.getChannels().stream()
                    .anyMatch(c -> c.getChannelName().equals(channelName));

            if (!exists) {
                config.getChannels().add(new ChannelConfig(channelName, true));
                configService.saveConfig();
                System.out.println("Neuer Channel automatisch hinzugefügt: " + channelName);
            }
        });
    }

    private void handleMultiplierUpdate(ActiveMultiplier multiplier) {
        if (!syncing) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("Multiplier Update empfangen");
        });
    }

    private void handlePause() {
        if (!syncing) {
            return;
        }

        if (timerManager.shouldIgnoreNextPauseSync()) {
            System.out.println("Pause-Sync ignoriert (eigene Action)");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (!timerManager.isPaused()) {
                timerManager.pause(false);
                System.out.println("Timer durch Server pausiert");
            }
        });
    }

    private void handleResume() {
        if (!syncing) {
            return;
        }

        if (timerManager.shouldIgnoreNextResumeSync()) {
            System.out.println("Resume-Sync ignoriert (eigene Action)");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (timerManager.isPaused()) {
                timerManager.resume(false);
                System.out.println("Timer durch Server fortgesetzt");
            }
        });
    }

    private void handleConnectionStatus(ServerConnectionStatus status) {
        if (status == ServerConnectionStatus.CONNECTED) {
            startSync();
            return;
        }

        if (status == ServerConnectionStatus.DISCONNECTED) {
            stopSync();
        }
    }
}
