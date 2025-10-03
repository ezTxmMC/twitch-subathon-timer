package de.syntaxjason.model;

public class ServerMessage {
    private String type;
    private String channelName;
    private Object data;
    private long timestamp;

    public ServerMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ServerMessage(String type, String channelName, Object data) {
        this.type = type;
        this.channelName = channelName;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
