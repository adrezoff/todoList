package org.example.bot.handlers.dialogs;

import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Dialog {
    UserState getSupportedState();
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException;
}
