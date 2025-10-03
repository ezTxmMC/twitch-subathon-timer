package de.syntaxjason.service.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.*;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.*;
import de.syntaxjason.service.config.IConfigService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TwitchService implements ITwitchService {
    private TwitchClient twitchClient;
    private final IConfigService configService;
    private final ITimerManager timerManager;
    private final String myChannelName;
    private final List<String> joinedChannels;
    private boolean connected = false;
    private final List<Consumer<Boolean>> connectionStatusListeners;

    public TwitchService(IConfigService configService, ITimerManager timerManager, String myChannelName) {
        this.configService = configService;
        this.timerManager = timerManager;
        this.myChannelName = myChannelName;
        this.joinedChannels = new ArrayList<>();
        this.connectionStatusListeners = new ArrayList<>();
    }

    @Override
    public void connect() {
        SabathonConfig config = configService.getConfig();
        String botToken = config.getBotSettings().getBotAccessToken();

        if (botToken.isEmpty()) {
            System.out.println("Bot OAuth Token nicht konfiguriert. Twitch-Verbindung übersprungen.");
            updateConnectionStatus(false);
            return;
        }

        try {
            OAuth2Credential credential = new OAuth2Credential("twitch", botToken);

            twitchClient = TwitchClientBuilder.builder()
                    .withEnableChat(true)
                    .withChatAccount(credential)
                    .build();

            joinConfiguredChannels();
            registerEventHandlers();

            connected = true;
            updateConnectionStatus(true);
            System.out.println("Twitch Service verbunden als: " + myChannelName);

        } catch (Exception e) {
            System.err.println("Fehler beim Verbinden mit Twitch: " + e.getMessage());
            connected = false;
            updateConnectionStatus(false);
        }
    }

    @Override
    public void disconnect() {
        if (twitchClient == null) {
            return;
        }

        for (String channel : new ArrayList<>(joinedChannels)) {
            partChannel(channel);
        }

        twitchClient.close();
        twitchClient = null;
        joinedChannels.clear();
        connected = false;
        updateConnectionStatus(false);

        System.out.println("Twitch Service getrennt");
    }

    @Override
    public void joinChannel(String channelName) {
        if (twitchClient == null) {
            return;
        }

        if (joinedChannels.contains(channelName.toLowerCase())) {
            return;
        }

        twitchClient.getChat().joinChannel(channelName);
        joinedChannels.add(channelName.toLowerCase());

        System.out.println("Channel beigetreten: " + channelName);
    }

    @Override
    public void partChannel(String channelName) {
        if (twitchClient == null) {
            return;
        }

        if (!joinedChannels.contains(channelName.toLowerCase())) {
            return;
        }

        twitchClient.getChat().leaveChannel(channelName);
        joinedChannels.remove(channelName.toLowerCase());

        System.out.println("Channel verlassen: " + channelName);
    }

    @Override
    public List<String> getJoinedChannels() {
        return new ArrayList<>(joinedChannels);
    }

    @Override
    public boolean isConnected() {
        return connected && twitchClient != null;
    }

    @Override
    public void registerConnectionStatusListener(Consumer<Boolean> listener) {
        connectionStatusListeners.add(listener);
        listener.accept(connected);
    }

    private void updateConnectionStatus(boolean isConnected) {
        this.connected = isConnected;
        for (Consumer<Boolean> listener : connectionStatusListeners) {
            listener.accept(isConnected);
        }
    }

    private void joinConfiguredChannels() {
        SabathonConfig config = configService.getConfig();

        for (ChannelConfig channel : config.getChannels()) {
            if (channel.isActive()) {
                joinChannel(channel.getChannelName());
            }
        }
    }

    private void registerEventHandlers() {
        if (twitchClient == null) {
            return;
        }

        twitchClient.getEventManager().onEvent(FollowEvent.class, this::handleFollowEvent);
        twitchClient.getEventManager().onEvent(RaidEvent.class, this::handleRaidEvent);
        twitchClient.getEventManager().onEvent(SubscriptionEvent.class, this::handleSubscriptionEvent);
        twitchClient.getEventManager().onEvent(CheerEvent.class, this::handleCheerEvent);
        twitchClient.getEventManager().onEvent(GiftSubscriptionsEvent.class, this::handleGiftSubEvent);
    }

    private void handleFollowEvent(FollowEvent event) {
        SabathonConfig config = configService.getConfig();

        if (!config.getFollowerSettings().isEnabled()) {
            return;
        }

        String username = event.getUser().getName();
        String channelName = event.getChannel().getName();
        int minutes = config.getEventMinutes(EventType.FOLLOWER);

        TimerEvent timerEvent = new TimerEvent(EventType.FOLLOWER, username, channelName, minutes);
        timerManager.processEvent(timerEvent);

        System.out.println("Follow: " + username + " @ " + channelName + " +" + minutes + " Min");
    }

    private void handleRaidEvent(RaidEvent event) {
        SabathonConfig config = configService.getConfig();

        if (!config.getRaidSettings().isEnabled()) {
            return;
        }

        String username = event.getRaider().getName();
        String channelName = event.getChannel().getName();
        int viewerCount = event.getViewers();
        int minutes = calculateRaidMinutes(viewerCount);

        TimerEvent timerEvent = new TimerEvent(EventType.RAID, username, channelName, minutes, viewerCount);
        timerManager.processEvent(timerEvent);

        System.out.println("Raid: " + username + " @ " + channelName + " mit " + viewerCount + " Viewers +" + minutes + " Min");
    }

    private void handleSubscriptionEvent(SubscriptionEvent event) {
        SabathonConfig config = configService.getConfig();
        String username = event.getUser().getName();
        String channelName = event.getChannel().getName();
        int minutes = config.getEventMinutes(EventType.SUB);

        TimerEvent timerEvent = new TimerEvent(EventType.SUB, username, channelName, minutes);
        timerManager.processEvent(timerEvent);

        System.out.println("Sub: " + username + " @ " + channelName + " +" + minutes + " Min");
    }

    private void handleCheerEvent(CheerEvent event) {
        SabathonConfig config = configService.getConfig();

        if (!config.getBitsSettings().isEnabled()) {
            return;
        }

        String username = event.getUser().getName();
        String channelName = event.getChannel().getName();
        int bits = event.getBits();
        int minutes = calculateBitsMinutes(bits);

        TimerEvent timerEvent = new TimerEvent(EventType.BITS, username, channelName, minutes, bits);
        timerManager.processEvent(timerEvent);

        System.out.println("Bits: " + username + " @ " + channelName + " mit " + bits + " Bits +" + minutes + " Min");
    }

    private void handleGiftSubEvent(GiftSubscriptionsEvent event) {
        SabathonConfig config = configService.getConfig();
        String username = event.getUser().getName();
        String channelName = event.getChannel().getName();
        int count = event.getCount();
        int minutes = config.getEventMinutes(EventType.SUBGIFT) * count;

        TimerEvent timerEvent = new TimerEvent(EventType.SUBGIFT, username, channelName, minutes, count);
        timerManager.processEvent(timerEvent);

        System.out.println("Gift Subs: " + username + " @ " + channelName + " x" + count + " +" + minutes + " Min");
    }

    private int calculateBitsMinutes(int bits) {
        SabathonConfig config = configService.getConfig();
        SabathonConfig.BitsSettings settings = config.getBitsSettings();

        return (bits / settings.getBitsPerMinute()) * settings.getMinutesPerThreshold();
    }

    private int calculateRaidMinutes(int viewers) {
        SabathonConfig config = configService.getConfig();
        SabathonConfig.RaidSettings settings = config.getRaidSettings();

        return (viewers / settings.getViewersPerMinute()) * settings.getMinutesPerThreshold();
    }
}
