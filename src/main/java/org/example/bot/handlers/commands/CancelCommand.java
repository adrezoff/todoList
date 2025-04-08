package org.example.bot.handlers.commands;

import org.example.bot.utils.MessageSender;
import org.example.bot.utils.UserState;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CancelCommand implements Command{

    @Override
    public String getName() {
        return "/cancel";
    }

    @Override
    public void execute(User user, long chatId,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        user.setState(UserState.IDLE);
        user.clearTempData();
        userRepository.update(user);
        messageSender.sendMessage(chatId, "Действие отменено");
    }
}
