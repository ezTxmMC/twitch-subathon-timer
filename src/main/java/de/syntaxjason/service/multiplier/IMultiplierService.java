package de.syntaxjason.service.multiplier;

import de.syntaxjason.model.ActiveMultiplier;
import de.syntaxjason.model.MultiplierReward;
import java.util.function.Consumer;

public interface IMultiplierService {
    void activateMultiplier(MultiplierReward reward, String username);
    double getCurrentMultiplier();
    ActiveMultiplier getActiveMultiplier();
    boolean hasActiveMultiplier();
    void clearExpiredMultipliers();
    void registerMultiplierListener(Consumer<ActiveMultiplier> listener);
}
