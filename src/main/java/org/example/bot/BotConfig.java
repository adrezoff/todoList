package org.example.bot;

public class BotConfig {
    private static final String BOT_TOKEN = System.getenv("API_KEY");
    private static final String BOT_NAME = System.getenv("BOT_NAME");

    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken(){
        return BOT_TOKEN;
    }
}
