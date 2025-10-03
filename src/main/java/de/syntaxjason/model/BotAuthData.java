package de.syntaxjason.model;

public class BotAuthData {
    private String botAccessToken;

    public BotAuthData() {
    }

    public BotAuthData(String botAccessToken) {
        this.botAccessToken = botAccessToken;
    }

    public String getBotAccessToken() {
        return botAccessToken;
    }

    public void setBotAccessToken(String botAccessToken) {
        this.botAccessToken = botAccessToken;
    }
}
