package de.syntaxjason.handler.events;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.GiftSubscriptionsEvent;
import de.syntaxjason.handler.IEventHandler;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.database.IDatabaseService;

public class SubgiftEventHandler implements IEventHandler {
    private final ITimerManager timerManager;
    private final IDatabaseService databaseService;
    private final IConfigService configService;

    public SubgiftEventHandler(ITimerManager timerManager, IDatabaseService databaseService, IConfigService configService) {
        this.timerManager = timerManager;
        this.databaseService = databaseService;
        this.configService = configService;
    }

    @Override
    public void register(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(GiftSubscriptionsEvent.class, this::handleEvent);
    }

    private void handleEvent(GiftSubscriptionsEvent event) {
        int giftsCount = event.getTotalCount();
        int minutesPerGift = configService.getConfig().getEventMinutes(EventType.SUBGIFT);
        int totalMinutes = giftsCount * minutesPerGift;

        TimerEvent timerEvent = new TimerEvent.Builder()
                .eventType(EventType.SUBGIFT)
                .username(event.getUser().getName())
                .minutesAdded(totalMinutes)
                .details(String.format("%d Subgifts (%d x %d Min = %d Min)",
                        giftsCount,
                        giftsCount,
                        minutesPerGift,
                        totalMinutes))
                .build();

        timerManager.processEvent(timerEvent);
        databaseService.saveEvent(timerEvent);
    }
}
