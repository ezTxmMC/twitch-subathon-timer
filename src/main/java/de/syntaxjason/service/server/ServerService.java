package de.syntaxjason.service.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.model.*;
import de.syntaxjason.util.LocalDateTimeAdapter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ServerService implements IServerService {
    private Channel channel;
    private EventLoopGroup group;
    private ServerConnectionStatus connectionStatus;
    private final String myChannelName;
    private final Gson gson;
    private final List<ParticipantInfo> participants;
    private OBSOverlayInfo obsOverlayInfo;

    private String currentSessionId;

    private final List<Consumer<ServerConnectionStatus>> statusListeners;
    private final List<Consumer<TimerStateUpdate>> timerUpdateListeners;
    private final List<Consumer<TimerEvent>> eventListeners;
    private final List<Consumer<List<ParticipantInfo>>> participantListeners;
    private final List<Consumer<SessionSyncData>> fullSyncListeners;
    private final List<Consumer<SabathonConfig>> configUpdateListeners;
    private final List<Consumer<String>> channelAddListeners;
    private final List<Consumer<ActiveMultiplier>> multiplierListeners;
    private final List<Consumer<OBSOverlayInfo>> obsOverlayListeners;
    private final List<Runnable> pauseListeners;
    private final List<Runnable> resumeListeners;

    public ServerService(String myChannelName) {
        this.myChannelName = myChannelName;
        this.connectionStatus = ServerConnectionStatus.DISCONNECTED;
        this.participants = new CopyOnWriteArrayList<>();
        this.statusListeners = new ArrayList<>();
        this.timerUpdateListeners = new ArrayList<>();
        this.eventListeners = new ArrayList<>();
        this.participantListeners = new ArrayList<>();
        this.fullSyncListeners = new ArrayList<>();
        this.configUpdateListeners = new ArrayList<>();
        this.channelAddListeners = new ArrayList<>();
        this.multiplierListeners = new ArrayList<>();
        this.obsOverlayListeners = new ArrayList<>();
        this.pauseListeners = new ArrayList<>();
        this.resumeListeners = new ArrayList<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    public void connect(String serverUrl, String sessionId) {
        if (isConnected()) {
            return;
        }

        this.currentSessionId = sessionId;

        updateStatus(ServerConnectionStatus.CONNECTING);

        try {
            URI uri = new URI(String.format("%s/session/%s?channel=%s", serverUrl, sessionId, myChannelName));
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            boolean ssl = "wss".equalsIgnoreCase(scheme);
            SslContext sslContext = createSslContext(ssl);

            group = new NioEventLoopGroup();

            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

            WebSocketClientHandler handler = new WebSocketClientHandler(
                    handshaker,
                    this::handleServerMessage,
                    this::handleConnectionChange,
                    gson
            );

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(ch.alloc(), host, port));
                            }

                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            pipeline.addLast(handler);
                        }
                    });

            channel = bootstrap.connect(host, port).sync().channel();
            handler.handshakeFuture().sync();

            updateStatus(ServerConnectionStatus.CONNECTED);
            System.out.println("Verbunden mit Server: " + serverUrl);

        } catch (Exception e) {
            updateStatus(ServerConnectionStatus.ERROR);
            System.err.println("Fehler beim Verbinden zum Server: " + e.getMessage());
            e.printStackTrace();

            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }

    private SslContext createSslContext(boolean ssl) throws Exception {
        if (!ssl) {
            return null;
        }

        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    @Override
    public void disconnect() {
        if (channel == null) {
            return;
        }

        try {
            sendMessage(MessageType.SESSION_LEAVE, null);
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.closeFuture().sync();

            participants.clear();
            currentSessionId = null;
            obsOverlayInfo = null;

            notifyParticipantListeners();

            updateStatus(ServerConnectionStatus.DISCONNECTED);
            System.out.println("Verbindung zum Server getrennt");
        } catch (InterruptedException e) {
            System.err.println("Fehler beim Trennen der Verbindung");
            e.printStackTrace();
        } finally {
            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }

    @Override
    public boolean isConnected() {
        if (channel == null) {
            return false;
        }

        return channel.isActive();
    }

    @Override
    public ServerConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    @Override
    public void createSession(String sessionName, long initialMinutes, SabathonConfig config) {
        SessionSyncData syncData = new SessionSyncData();
        syncData.setSessionName(sessionName);
        syncData.setInitialMinutes(initialMinutes);
        syncData.setRemainingMinutes(initialMinutes);
        syncData.setPaused(false);
        syncData.setConfig(config);
        syncData.setEventHistory(new ArrayList<>());

        sendMessage(MessageType.SESSION_CREATE, syncData);
    }

    @Override
    public void sendSessionJoin() {
        sendMessage(MessageType.SESSION_JOIN, null);
    }

    @Override
    public void sendTimerUpdate(Duration remainingTime, boolean isPaused) {
        TimerStateUpdate update = new TimerStateUpdate(remainingTime.getSeconds(), isPaused);
        sendMessage(MessageType.TIMER_UPDATE, update);
    }

    @Override
    public void sendEvent(TimerEvent event) {
        sendMessage(MessageType.EVENT, event);
    }

    @Override
    public void sendPauseRequest() {
        sendMessage(MessageType.TIMER_PAUSE, null);
    }

    @Override
    public void sendResumeRequest() {
        sendMessage(MessageType.TIMER_RESUME, null);
    }

    @Override
    public void sendResetRequest() {
        sendMessage(MessageType.TIMER_RESET, null);
    }

    @Override
    public void sendAddMinutesRequest(int minutes) {
        sendMessage(MessageType.TIMER_ADD_MINUTES, minutes);
    }

    @Override
    public void sendConfigUpdate(SabathonConfig config) {
        sendMessage(MessageType.CONFIG_UPDATE, config);
    }

    @Override
    public void sendChannelAdd(String channelName) {
        sendMessage(MessageType.CHANNEL_ADD, channelName);
    }

    @Override
    public void sendMultiplierActivate(MultiplierReward reward, String activatedBy) {
        ActiveMultiplier multiplier = new ActiveMultiplier(
                reward.getMultiplier(),
                reward.getDurationMinutes(),
                activatedBy
        );
        sendMessage(MessageType.MULTIPLIER_ACTIVATE, multiplier);
    }

    @Override
    public List<ParticipantInfo> getConnectedParticipants() {
        return new ArrayList<>(participants);
    }

    @Override
    public OBSOverlayInfo getOBSOverlayInfo() {
        return obsOverlayInfo;
    }

    @Override
    public void registerConnectionStatusListener(Consumer<ServerConnectionStatus> listener) {
        statusListeners.add(listener);
    }

    @Override
    public void registerTimerUpdateListener(Consumer<TimerStateUpdate> listener) {
        timerUpdateListeners.add(listener);
    }

    @Override
    public void registerEventListener(Consumer<TimerEvent> listener) {
        eventListeners.add(listener);
    }

    @Override
    public void registerParticipantListener(Consumer<List<ParticipantInfo>> listener) {
        participantListeners.add(listener);
    }

    @Override
    public void registerFullSyncListener(Consumer<SessionSyncData> listener) {
        fullSyncListeners.add(listener);
    }

    @Override
    public void registerConfigUpdateListener(Consumer<SabathonConfig> listener) {
        configUpdateListeners.add(listener);
    }

    @Override
    public void registerChannelAddListener(Consumer<String> listener) {
        channelAddListeners.add(listener);
    }

    @Override
    public void registerMultiplierListener(Consumer<ActiveMultiplier> listener) {
        multiplierListeners.add(listener);
    }

    @Override
    public void registerOBSOverlayListener(Consumer<OBSOverlayInfo> listener) {
        obsOverlayListeners.add(listener);
    }

    @Override
    public void registerPauseListener(Runnable listener) {
        pauseListeners.add(listener);
    }

    @Override
    public void registerResumeListener(Runnable listener) {
        resumeListeners.add(listener);
    }

    private void sendMessage(String type, Object data) {
        if (!isConnected()) {
            System.err.println("Nicht verbunden - kann Nachricht nicht senden");
            return;
        }

        try {
            ServerMessage message = new ServerMessage(type, myChannelName, data);
            String json = gson.toJson(message);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        } catch (Exception e) {
            System.err.println("Fehler beim Senden der Nachricht: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConnectionChange(boolean connected) {
        if (connected) {
            updateStatus(ServerConnectionStatus.CONNECTED);
            return;
        }

        updateStatus(ServerConnectionStatus.DISCONNECTED);
    }

    private void handleServerMessage(ServerMessage message) {
        String type = message.getType();

        switch (type) {
            case MessageType.FULL_SYNC -> handleFullSync(message);
            case MessageType.TIMER_UPDATE -> handleTimerUpdate(message);
            case MessageType.TIMER_PAUSE -> notifyPause();
            case MessageType.TIMER_RESUME -> notifyResume();
            case MessageType.EVENT -> handleEvent(message);
            case MessageType.CONFIG_UPDATE -> handleConfigUpdate(message);
            case MessageType.CHANNEL_ADD -> handleChannelAdd(message);
            case MessageType.MULTIPLIER_ACTIVATE -> handleMultiplierActivate(message);
            case MessageType.MULTIPLIER_EXPIRE -> handleMultiplierExpire();
            case MessageType.PARTICIPANTS -> handleParticipants(message);
            case "OBS_OVERLAY_INFO" -> handleOBSOverlayInfo(message);
            default -> System.out.println("Unbekannter Nachrichtentyp: " + type);
        }
    }

    private void handleFullSync(ServerMessage message) {
        SessionSyncData syncData = gson.fromJson(gson.toJson(message.getData()), SessionSyncData.class);
        notifyFullSyncListeners(syncData);
    }

    private void handleTimerUpdate(ServerMessage message) {
        TimerStateUpdate update = gson.fromJson(gson.toJson(message.getData()), TimerStateUpdate.class);
        notifyTimerUpdateListeners(update);
    }

    private void handleEvent(ServerMessage message) {
        TimerEvent event = gson.fromJson(gson.toJson(message.getData()), TimerEvent.class);
        notifyEventListeners(event);
    }

    private void handleConfigUpdate(ServerMessage message) {
        SabathonConfig config = gson.fromJson(gson.toJson(message.getData()), SabathonConfig.class);
        notifyConfigUpdateListeners(config);
    }

    private void handleChannelAdd(ServerMessage message) {
        String channelName = gson.fromJson(gson.toJson(message.getData()), String.class);
        notifyChannelAddListeners(channelName);
    }

    private void handleMultiplierActivate(ServerMessage message) {
        ActiveMultiplier multiplier = gson.fromJson(gson.toJson(message.getData()), ActiveMultiplier.class);
        notifyMultiplierListeners(multiplier);
    }

    private void handleMultiplierExpire() {
        notifyMultiplierListeners(null);
    }

    private void handleParticipants(ServerMessage message) {
        participants.clear();

        @SuppressWarnings("unchecked")
        List<ParticipantInfo> participantList = gson.fromJson(
                gson.toJson(message.getData()),
                new com.google.gson.reflect.TypeToken<List<ParticipantInfo>>(){}.getType()
        );

        participants.addAll(participantList);
        notifyParticipantListeners();
    }

    private void handleOBSOverlayInfo(ServerMessage message) {
        obsOverlayInfo = gson.fromJson(gson.toJson(message.getData()), OBSOverlayInfo.class);
        notifyOBSOverlayListeners(obsOverlayInfo);
        System.out.println("OBS Overlay URL empfangen: " + obsOverlayInfo.getFullUrl());
    }

    private void notifyPause() {
        System.out.println("Server: Timer pausiert");
        for (Runnable listener : pauseListeners) {
            listener.run();
        }
    }

    private void notifyResume() {
        System.out.println("Server: Timer fortgesetzt");
        for (Runnable listener : resumeListeners) {
            listener.run();
        }
    }

    private void updateStatus(ServerConnectionStatus status) {
        this.connectionStatus = status;
        notifyStatusListeners(status);
    }

    private void notifyStatusListeners(ServerConnectionStatus status) {
        for (Consumer<ServerConnectionStatus> listener : statusListeners) {
            listener.accept(status);
        }
    }

    private void notifyTimerUpdateListeners(TimerStateUpdate update) {
        for (Consumer<TimerStateUpdate> listener : timerUpdateListeners) {
            listener.accept(update);
        }
    }

    private void notifyEventListeners(TimerEvent event) {
        for (Consumer<TimerEvent> listener : eventListeners) {
            listener.accept(event);
        }
    }

    private void notifyParticipantListeners() {
        for (Consumer<List<ParticipantInfo>> listener : participantListeners) {
            listener.accept(new ArrayList<>(participants));
        }
    }

    private void notifyFullSyncListeners(SessionSyncData syncData) {
        for (Consumer<SessionSyncData> listener : fullSyncListeners) {
            listener.accept(syncData);
        }
    }

    private void notifyConfigUpdateListeners(SabathonConfig config) {
        for (Consumer<SabathonConfig> listener : configUpdateListeners) {
            listener.accept(config);
        }
    }

    private void notifyChannelAddListeners(String channelName) {
        for (Consumer<String> listener : channelAddListeners) {
            listener.accept(channelName);
        }
    }

    private void notifyMultiplierListeners(ActiveMultiplier multiplier) {
        for (Consumer<ActiveMultiplier> listener : multiplierListeners) {
            listener.accept(multiplier);
        }
    }

    private void notifyOBSOverlayListeners(OBSOverlayInfo info) {
        for (Consumer<OBSOverlayInfo> listener : obsOverlayListeners) {
            listener.accept(info);
        }
    }

    @Override
    public String getCurrentSessionId() {
        return currentSessionId;
    }
}
