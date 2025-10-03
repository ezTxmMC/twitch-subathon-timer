package de.syntaxjason.model;

public class MultiplierReward {
    private String rewardId;
    private double multiplier;
    private int durationMinutes;

    public MultiplierReward() {}

    public MultiplierReward(String rewardId, double multiplier, int durationMinutes) {
        this.rewardId = rewardId;
        this.multiplier = multiplier;
        this.durationMinutes = durationMinutes;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
