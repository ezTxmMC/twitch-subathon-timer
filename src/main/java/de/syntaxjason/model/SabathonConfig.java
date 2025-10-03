package de.syntaxjason.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SabathonConfig {
    private BotSettings botSettings;
    private List<ChannelConfig> channels;
    private TimerSettings timerSettings;
    private Map<EventType, Integer> eventMinutes;
    private BitsSettings bitsSettings;
    private RaidSettings raidSettings;
    private FollowerSettings followerSettings;
    private DatabaseSettings databaseSettings;
    private UISettings uiSettings;
    private LoggingSettings loggingSettings;
    private List<MultiplierReward> multiplierRewards;

    public SabathonConfig() {
        this.botSettings = new BotSettings();
        this.channels = new ArrayList<>();
        this.timerSettings = new TimerSettings();
        this.eventMinutes = new HashMap<>();
        this.bitsSettings = new BitsSettings();
        this.raidSettings = new RaidSettings();
        this.followerSettings = new FollowerSettings();
        this.databaseSettings = new DatabaseSettings();
        this.uiSettings = new UISettings();
        this.loggingSettings = new LoggingSettings();
        this.multiplierRewards = new ArrayList<>();
        initializeDefaultEventMinutes();
        initializeDefaultMultipliers();
    }

    private void initializeDefaultEventMinutes() {
        eventMinutes.put(EventType.FOLLOWER, 2);
        eventMinutes.put(EventType.RAID, 5);
        eventMinutes.put(EventType.SUB, 10);
        eventMinutes.put(EventType.BITS, 5);
        eventMinutes.put(EventType.SUBGIFT, 10);
    }

    private void initializeDefaultMultipliers() {
        multiplierRewards.add(new MultiplierReward("reward-id-1", 2.0, 5));
        multiplierRewards.add(new MultiplierReward("reward-id-2", 3.0, 10));
    }

    // Getters and Setters

    public BotSettings getBotSettings() {
        return botSettings;
    }

    public void setBotSettings(BotSettings botSettings) {
        this.botSettings = botSettings;
    }

    public List<ChannelConfig> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelConfig> channels) {
        this.channels = channels;
    }

    public TimerSettings getTimerSettings() {
        return timerSettings;
    }

    public void setTimerSettings(TimerSettings timerSettings) {
        this.timerSettings = timerSettings;
    }

    public int getEventMinutes(EventType eventType) {
        return eventMinutes.getOrDefault(eventType, 0);
    }

    public void setEventMinutes(EventType eventType, int minutes) {
        eventMinutes.put(eventType, minutes);
    }

    public Map<EventType, Integer> getAllEventMinutes() {
        return new HashMap<>(eventMinutes);
    }

    public BitsSettings getBitsSettings() {
        return bitsSettings;
    }

    public void setBitsSettings(BitsSettings bitsSettings) {
        this.bitsSettings = bitsSettings;
    }

    public RaidSettings getRaidSettings() {
        return raidSettings;
    }

    public void setRaidSettings(RaidSettings raidSettings) {
        this.raidSettings = raidSettings;
    }

    public FollowerSettings getFollowerSettings() {
        return followerSettings;
    }

    public void setFollowerSettings(FollowerSettings followerSettings) {
        this.followerSettings = followerSettings;
    }

    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    public void setDatabaseSettings(DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
    }

    public UISettings getUiSettings() {
        return uiSettings;
    }

    public void setUiSettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    public LoggingSettings getLoggingSettings() {
        return loggingSettings;
    }

    public void setLoggingSettings(LoggingSettings loggingSettings) {
        this.loggingSettings = loggingSettings;
    }

    public List<MultiplierReward> getMultiplierRewards() {
        return multiplierRewards;
    }

    public void setMultiplierRewards(List<MultiplierReward> multiplierRewards) {
        this.multiplierRewards = multiplierRewards;
    }

    public MultiplierReward getMultiplierRewardById(String rewardId) {
        if (rewardId == null) {
            return null;
        }

        for (MultiplierReward reward : multiplierRewards) {
            if (rewardId.equals(reward.getRewardId())) {
                return reward;
            }
        }

        return null;
    }

    public static class BotSettings {
        private String botAccessToken = "";

        public String getBotAccessToken() {
            return botAccessToken;
        }

        public void setBotAccessToken(String botAccessToken) {
            this.botAccessToken = botAccessToken;
        }
    }

    public static class TimerSettings {
        private long totalMinutes = 240;
        private boolean autoStart = true;

        public long getTotalMinutes() {
            return totalMinutes;
        }

        public void setTotalMinutes(long totalMinutes) {
            this.totalMinutes = totalMinutes;
        }

        public boolean isAutoStart() {
            return autoStart;
        }

        public void setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
        }
    }

    public static class BitsSettings {
        private int threshold = 100;
        private boolean roundDown = true;
        private int minimumBits = 0;

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }

        public boolean isRoundDown() {
            return roundDown;
        }

        public void setRoundDown(boolean roundDown) {
            this.roundDown = roundDown;
        }

        public int getMinimumBits() {
            return minimumBits;
        }

        public void setMinimumBits(int minimumBits) {
            this.minimumBits = minimumBits;
        }
    }

    public static class RaidSettings {
        private int minimumViewers = 1;
        private boolean preventDuplicates = true;
        private int duplicateCheckHours = 12;

        public int getMinimumViewers() {
            return minimumViewers;
        }

        public void setMinimumViewers(int minimumViewers) {
            this.minimumViewers = minimumViewers;
        }

        public boolean isPreventDuplicates() {
            return preventDuplicates;
        }

        public void setPreventDuplicates(boolean preventDuplicates) {
            this.preventDuplicates = preventDuplicates;
        }

        public int getDuplicateCheckHours() {
            return duplicateCheckHours;
        }

        public void setDuplicateCheckHours(int duplicateCheckHours) {
            this.duplicateCheckHours = duplicateCheckHours;
        }
    }

    public static class FollowerSettings {
        private boolean preventRefollows = true;

        public boolean isPreventRefollows() {
            return preventRefollows;
        }

        public void setPreventRefollows(boolean preventRefollows) {
            this.preventRefollows = preventRefollows;
        }
    }

    public static class DatabaseSettings {
        private int maxPoolSize = 5;
        private int minIdleConnections = 1;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private boolean autoCommit = false;
        private boolean enableBackups = true;
        private int backupIntervalHours = 24;

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getMinIdleConnections() {
            return minIdleConnections;
        }

        public void setMinIdleConnections(int minIdleConnections) {
            this.minIdleConnections = minIdleConnections;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public long getMaxLifetime() {
            return maxLifetime;
        }

        public void setMaxLifetime(long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }

        public boolean isAutoCommit() {
            return autoCommit;
        }

        public void setAutoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
        }

        public boolean isEnableBackups() {
            return enableBackups;
        }

        public void setEnableBackups(boolean enableBackups) {
            this.enableBackups = enableBackups;
        }

        public int getBackupIntervalHours() {
            return backupIntervalHours;
        }

        public void setBackupIntervalHours(int backupIntervalHours) {
            this.backupIntervalHours = backupIntervalHours;
        }
    }

    public static class UISettings {
        private int windowWidth = 1200;
        private int windowHeight = 800;
        private int fontSize = 14;
        private int timerFontSize = 96;
        private boolean darkTheme = true;
        private boolean alwaysOnTop = false;
        private int maxLogEntries = 1000;

        public int getWindowWidth() {
            return windowWidth;
        }

        public void setWindowWidth(int windowWidth) {
            this.windowWidth = windowWidth;
        }

        public int getWindowHeight() {
            return windowHeight;
        }

        public void setWindowHeight(int windowHeight) {
            this.windowHeight = windowHeight;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

        public int getTimerFontSize() {
            return timerFontSize;
        }

        public void setTimerFontSize(int timerFontSize) {
            this.timerFontSize = timerFontSize;
        }

        public boolean isDarkTheme() {
            return darkTheme;
        }

        public void setDarkTheme(boolean darkTheme) {
            this.darkTheme = darkTheme;
        }

        public boolean isAlwaysOnTop() {
            return alwaysOnTop;
        }

        public void setAlwaysOnTop(boolean alwaysOnTop) {
            this.alwaysOnTop = alwaysOnTop;
        }

        public int getMaxLogEntries() {
            return maxLogEntries;
        }

        public void setMaxLogEntries(int maxLogEntries) {
            this.maxLogEntries = maxLogEntries;
        }
    }

    public static class LoggingSettings {
        private boolean enableFileLogging = true;
        private boolean enableConsoleLogging = true;
        private String logLevel = "INFO";
        private int maxLogFiles = 10;
        private int maxLogSizeMB = 10;

        public boolean isEnableFileLogging() {
            return enableFileLogging;
        }

        public void setEnableFileLogging(boolean enableFileLogging) {
            this.enableFileLogging = enableFileLogging;
        }

        public boolean isEnableConsoleLogging() {
            return enableConsoleLogging;
        }

        public void setEnableConsoleLogging(boolean enableConsoleLogging) {
            this.enableConsoleLogging = enableConsoleLogging;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public int getMaxLogFiles() {
            return maxLogFiles;
        }

        public void setMaxLogFiles(int maxLogFiles) {
            this.maxLogFiles = maxLogFiles;
        }

        public int getMaxLogSizeMB() {
            return maxLogSizeMB;
        }

        public void setMaxLogSizeMB(int maxLogSizeMB) {
            this.maxLogSizeMB = maxLogSizeMB;
        }
    }
}
