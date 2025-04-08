package org.example.bot;

import org.example.bot.handlers.CallbackHandler;
import org.example.bot.handlers.MessageHandler;
import org.example.bot.utils.MessageSender;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private MessageHandler messageHandler;
    private CallbackHandler callbackHandler;

    public Bot(BotConfig botConfig, UserRepository userRepository, TaskRepository taskRepository) {
        super(botConfig.getBotToken());
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        initializeHandlers();
    }

    private void initializeHandlers() {
        MessageSender messageSender = MessageSender.getInstance(this);
        this.messageHandler = new MessageHandler(messageSender, userRepository, taskRepository);
        this.callbackHandler = new CallbackHandler(messageSender, userRepository, taskRepository);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                messageHandler.handle(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                callbackHandler.handle(update.getCallbackQuery());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }
}