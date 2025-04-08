package org.example;

import org.example.bot.Bot;
import org.example.bot.BotConfig;
import org.example.bot.handlers.MessageHandler;
import org.example.bot.utils.MessageSender;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        BotConfig botConfig = new BotConfig();
        UserRepository userRepository = new UserRepository();
        TaskRepository taskRepository = new TaskRepository();

        Bot bot = new Bot(botConfig, userRepository, taskRepository);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("bot started.");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}