package de.syntaxjason.service.server;

import de.syntaxjason.model.*;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public interface IServerService {
    void connect(String serverUrl, String sessionId);
    void disconnect();
    boolean isConnected();
    ServerConnectionStatus getConnectionStatus();
    String getCurrentSessionId();

    void createSession(String sessionName, long initialMinutes, SabathonConfig config);
    void sendSessionJoin();
    void sendTimerUpdate(Duration remainingTime, boolean isPaused);
    void sendEvent(TimerEvent event);
    void sendPauseRequest();
    void sendResumeRequest();
    void sendResetRequest();
    void sendAddMinutesRequest(int minutes);

    void sendConfigUpdate(SabathonConfig config);
    void sendChannelAdd(String channelName);
    void sendMultiplierActivate(MultiplierReward reward, String activatedBy);

    List<ParticipantInfo> getConnectedParticipants();
    OBSOverlayInfo getOBSOverlayInfo();

    void registerConnectionStatusListener(Consumer<ServerConnectionStatus> listener);
    void registerTimerUpdateListener(Consumer<TimerStateUpdate> listener);
    void registerEventListener(Consumer<TimerEvent> listener);
    void registerParticipantListener(Consumer<List<ParticipantInfo>> listener);
    void registerFullSyncListener(Consumer<SessionSyncData> listener);
    void registerConfigUpdateListener(Consumer<SabathonConfig> listener);
    void registerChannelAddListener(Consumer<String> listener);
    void registerMultiplierListener(Consumer<ActiveMultiplier> listener);
    void registerOBSOverlayListener(Consumer<OBSOverlayInfo> listener);
    void registerPauseListener(Runnable listener);
    void registerResumeListener(Runnable listener);
}
