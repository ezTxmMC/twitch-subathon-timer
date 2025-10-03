package de.syntaxjason.handler;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import de.syntaxjason.model.MultiplierReward;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.multiplier.IMultiplierService;

public class ChannelPointsRedemptionHandler implements IEventHandler {
    private final IMultiplierService multiplierService;
    private final IConfigService configService;

    public ChannelPointsRedemptionHandler(IMultiplierService multiplierService, IConfigService configService) {
        this.multiplierService = multiplierService;
        this.configService = configService;
    }

    @Override
    public void register(TwitchClient twitchClient) {
        twitchClient.getEventManager().onEvent(RewardRedeemedEvent.class, this::handleRedemption);
    }

    private void handleRedemption(RewardRedeemedEvent event) {
        if (event == null || event.getRedemption() == null) {
            return;
        }

        String rewardId = event.getRedemption().getReward().getId();
        String username = event.getRedemption().getUser().getDisplayName();

        MultiplierReward reward = configService.getConfig().getMultiplierRewardById(rewardId);

        if (reward == null) {
            return;
        }

        multiplierService.activateMultiplier(reward, username);
    }
}

