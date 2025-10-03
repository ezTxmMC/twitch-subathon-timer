package de.syntaxjason.handler;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.timer.ITimerService;
import de.syntaxjason.util.TimeParser;

import java.time.Duration;
import java.util.Set;

public class CommandHandler implements IEventHandler {
    private final ITimerService timerService;
    private final IConfigService configService;
    private final Set<String> allowedUsers;

    public CommandHandler(ITimerService timerService, IConfigService configService) {
        this.timerService = timerService;
        this.configService = configService;
        this.allowedUsers = Set.of("broadcaster", "moderator");
    }

    @Override
    public void register(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, this::handleMessage);
    }

    private void handleMessage(ChannelMessageEvent event) {
        if (event == null || event.getMessage() == null) {
            return;
        }

        String message = event.getMessage().trim();

        if (!message.startsWith("!timer")) {
            return;
        }

        if (!hasPermission(event)) {
            return;
        }

        String[] parts = message.split("\\s+", 3);

        if (parts.length < 2) {
            sendMessage(event, "Verwendung: !timer add/remove <Zeit> (z.B. !timer add 5m 30s)");
            return;
        }

        String subCommand = parts[1].toLowerCase();

        if (parts.length < 3) {
            sendMessage(event, "Bitte gib eine Zeit an (z.B. 5d 3h 30m 15s)");
            return;
        }

        String timeString = parts[2];

        if (subCommand.equals("add")) {
            handleTimerAdd(event, timeString);
            return;
        }

        if (subCommand.equals("remove")) {
            handleTimerRemove(event, timeString);
            return;
        }

        sendMessage(event, "Unbekannter Befehl. Verwende: !timer add/remove <Zeit>");
    }

    private void handleTimerAdd(ChannelMessageEvent event, String timeString) {
        try {
            Duration duration = TimeParser.parseTimeString(timeString);
            long minutes = duration.toMinutes();

            timerService.addMinutes((int) minutes);

            String formatted = TimeParser.formatDuration(duration);
            sendMessage(event, String.format("✅ %s zum Timer hinzugefügt", formatted));

        } catch (IllegalArgumentException e) {
            sendMessage(event, "❌ " + e.getMessage());
        }
    }

    private void handleTimerRemove(ChannelMessageEvent event, String timeString) {
        try {
            Duration duration = TimeParser.parseTimeString(timeString);
            long minutes = duration.toMinutes();

            Duration currentTime = timerService.getRemainingTime();
            Duration newTime = currentTime.minus(duration);

            if (newTime.isNegative()) {
                timerService.setRemainingTime(Duration.ZERO);
                sendMessage(event, "⚠️ Timer auf 0 gesetzt (Zeit wurde zu stark reduziert)");
                return;
            }

            timerService.setRemainingTime(newTime);

            String formatted = TimeParser.formatDuration(duration);
            sendMessage(event, String.format("✅ %s vom Timer entfernt", formatted));

        } catch (IllegalArgumentException e) {
            sendMessage(event, "❌ " + e.getMessage());
        }
    }

    private boolean hasPermission(ChannelMessageEvent event) {
        Set<CommandPermission> badges = event.getPermissions();

        for (String allowedRole : allowedUsers) {
            if (badges.stream().anyMatch(cp -> cp.name().equalsIgnoreCase(allowedRole))) {
                return true;
            }
        }

        return false;
    }

    private void sendMessage(ChannelMessageEvent event, String message) {
        event.getTwitchChat().sendMessage(
                event.getChannel().getName(),
                message
        );
    }
}

