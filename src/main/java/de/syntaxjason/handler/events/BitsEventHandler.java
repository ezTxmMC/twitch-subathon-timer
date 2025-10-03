package de.syntaxjason.handler.events;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.events.ChannelCheerEvent;
import de.syntaxjason.handler.IEventHandler;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.database.IDatabaseService;

public class BitsEventHandler implements IEventHandler {
    private final ITimerManager timerManager;
    private final IDatabaseService databaseService;
    private final IConfigService configService;

    public BitsEventHandler(ITimerManager timerManager, IDatabaseService databaseService, IConfigService configService) {
        this.timerManager = timerManager;
        this.databaseService = databaseService;
        this.configService = configService;
    }

    @Override
    public void register(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(ChannelCheerEvent.class, this::handleEvent);
    }

    private void handleEvent(ChannelCheerEvent event) {
        int totalBits = event.getBits();
        SabathonConfig.BitsSettings bitsSettings = configService.getConfig().getBitsSettings();

        if (totalBits < bitsSettings.getMinimumBits()) {
            return;
        }

        int threshold = bitsSettings.getThreshold();
        int minutesPerThreshold = configService.getConfig().getEventMinutes(EventType.BITS);
        int totalMinutes = (totalBits / threshold) * minutesPerThreshold;

        if (totalMinutes <= 0) {
            return;
        }

        TimerEvent timerEvent = new TimerEvent.Builder()
                .eventType(EventType.BITS)
                .username(event.getUserName())
                .minutesAdded(totalMinutes)
                .details(String.format("%d Bits (%d x %d Bits = %d Min)",
                        totalBits,
                        totalBits / threshold,
                        threshold,
                        totalMinutes))
                .build();

        timerManager.processEvent(timerEvent);
        databaseService.saveEvent(timerEvent);
    }
}
