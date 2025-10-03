package de.syntaxjason.model;

public class ParticipantInfo {
    private String channelName;
    private long joinedAt;
    private boolean isConnected;

    public ParticipantInfo() {
    }

    public ParticipantInfo(String channelName) {
        this.channelName = channelName;
        this.joinedAt = System.currentTimeMillis();
        this.isConnected = true;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
