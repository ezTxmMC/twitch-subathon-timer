package de.syntaxjason.handler.events;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import de.syntaxjason.handler.IEventHandler;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.database.IDatabaseService;

public class RaidEventHandler implements IEventHandler {

    private final ITimerManager timerManager;
    private final IDatabaseService databaseService;
    private final IConfigService configService;

    public RaidEventHandler(ITimerManager timerManager, IDatabaseService databaseService, IConfigService configService) {
        this.timerManager = timerManager;
        this.databaseService = databaseService;
        this.configService = configService;
    }

    @Override
    public void register(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(ChannelRaidEvent.class, this::handleEvent);
    }

    private void handleEvent(ChannelRaidEvent event) {
        int minutes = configService.getConfig().getEventMinutes(EventType.RAID);

        TimerEvent timerEvent = new TimerEvent.Builder()
                .eventType(EventType.RAID)
                .username(event.getFromBroadcasterUserName())
                .minutesAdded(minutes)
                .details("Viewers: " + event.getViewers())
                .build();

        timerManager.processEvent(timerEvent);
        databaseService.saveEvent(timerEvent);
    }
}
