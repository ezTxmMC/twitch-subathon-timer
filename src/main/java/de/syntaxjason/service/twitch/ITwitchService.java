package de.syntaxjason.service.twitch;

import java.util.List;
import java.util.function.Consumer;

public interface ITwitchService {
    void connect();
    void disconnect();
    void joinChannel(String channelName);
    void partChannel(String channelName);
    List<String> getJoinedChannels();
    boolean isConnected(); // NEU
    void registerConnectionStatusListener(Consumer<Boolean> listener); // NEU
}
