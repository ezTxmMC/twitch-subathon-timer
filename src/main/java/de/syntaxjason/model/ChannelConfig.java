package de.syntaxjason.model;

public class ChannelConfig {
    private String channelName;
    private boolean enabled;

    public ChannelConfig() {
    }

    public ChannelConfig(String channelName, boolean enabled) {
        this.channelName = channelName;
        this.enabled = enabled;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
