package de.syntaxjason.model;

public class MessageType {
    public static final String SESSION_CREATE = "SESSION_CREATE";
    public static final String SESSION_JOIN = "SESSION_JOIN";
    public static final String SESSION_LEAVE = "SESSION_LEAVE";

    public static final String TIMER_UPDATE = "TIMER_UPDATE";
    public static final String TIMER_PAUSE = "TIMER_PAUSE";
    public static final String TIMER_RESUME = "TIMER_RESUME";
    public static final String TIMER_RESET = "TIMER_RESET";
    public static final String TIMER_ADD_MINUTES = "TIMER_ADD_MINUTES";

    public static final String EVENT = "EVENT";
    public static final String EVENT_HISTORY = "EVENT_HISTORY";

    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String CONFIG_SYNC = "CONFIG_SYNC";
    public static final String BOT_AUTH = "BOT_AUTH";
    public static final String CHANNEL_ADD = "CHANNEL_ADD";

    public static final String MULTIPLIER_ACTIVATE = "MULTIPLIER_ACTIVATE";
    public static final String MULTIPLIER_EXPIRE = "MULTIPLIER_EXPIRE";

    public static final String PARTICIPANTS = "PARTICIPANTS";
    public static final String FULL_SYNC = "FULL_SYNC";

    public static final String OBS_OVERLAY_INFO = "OBS_OVERLAY_INFO";
}
