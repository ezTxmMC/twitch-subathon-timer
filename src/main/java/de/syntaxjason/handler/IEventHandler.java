package de.syntaxjason.handler;

import com.github.twitch4j.TwitchClient;

public interface IEventHandler {
    void register(TwitchClient twitchClient);
}
