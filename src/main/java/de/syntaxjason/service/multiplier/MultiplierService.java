package de.syntaxjason.service.multiplier;

import de.syntaxjason.model.ActiveMultiplier;
import de.syntaxjason.model.MultiplierReward;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MultiplierService implements IMultiplierService {
    private ActiveMultiplier activeMultiplier;
    private final List<Consumer<ActiveMultiplier>> multiplierListeners;

    public MultiplierService() {
        this.multiplierListeners = new ArrayList<>();
    }

    @Override
    public void activateMultiplier(MultiplierReward reward, String username) {
        if (reward == null) {
            return;
        }

        this.activeMultiplier = new ActiveMultiplier(
                reward.getMultiplier(),
                reward.getDurationMinutes(),
                username
        );

        notifyMultiplierListeners();

        System.out.println(String.format(
                "Multiplier aktiviert: %.1fx für %d Minuten von %s",
                reward.getMultiplier(),
                reward.getDurationMinutes(),
                username
        ));
    }

    @Override
    public double getCurrentMultiplier() {
        if (activeMultiplier == null) {
            return 1.0;
        }

        if (!activeMultiplier.isActive()) {
            return 1.0;
        }

        return activeMultiplier.getMultiplier();
    }

    @Override
    public ActiveMultiplier getActiveMultiplier() {
        if (activeMultiplier == null) {
            return null;
        }

        if (!activeMultiplier.isActive()) {
            return null;
        }

        return activeMultiplier;
    }

    @Override
    public boolean hasActiveMultiplier() {
        if (activeMultiplier == null) {
            return false;
        }

        return activeMultiplier.isActive();
    }

    @Override
    public void clearExpiredMultipliers() {
        if (activeMultiplier == null) {
            return;
        }

        if (activeMultiplier.isActive()) {
            return;
        }

        System.out.println("Multiplier abgelaufen");
        activeMultiplier = null;
        notifyMultiplierListeners();
    }

    @Override
    public void registerMultiplierListener(Consumer<ActiveMultiplier> listener) {
        multiplierListeners.add(listener);
    }

    private void notifyMultiplierListeners() {
        for (Consumer<ActiveMultiplier> listener : multiplierListeners) {
            listener.accept(activeMultiplier);
        }
    }
}
