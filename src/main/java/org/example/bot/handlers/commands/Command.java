package org.example.bot.handlers.commands;

import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface Command {
    public String getName();
    public void execute(User user, long chatId,
                           MessageSender messageSender,
                           UserRepository userRepository,
                           TaskRepository taskRepository) throws TelegramApiException;
}
